/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.coffeepick.gui.controller;

import com.io7m.coffeepick.api.CoffeePickClientProviderType;
import com.io7m.coffeepick.api.CoffeePickClientType;
import com.io7m.coffeepick.gui.controller.internal.CGXAbstractTask;
import com.io7m.coffeepick.gui.controller.internal.CGXBootTask;
import com.io7m.coffeepick.gui.controller.internal.CGXCatalogDownloadRuntimesTask;
import com.io7m.coffeepick.gui.controller.internal.CGXControllerInternalType;
import com.io7m.coffeepick.gui.controller.internal.CGXControllerStrings;
import com.io7m.coffeepick.gui.controller.internal.CGXControllerStringsType;
import com.io7m.coffeepick.gui.controller.internal.CGXDebugFailTask;
import com.io7m.coffeepick.gui.controller.internal.CGXInventoryDeleteTask;
import com.io7m.coffeepick.gui.controller.internal.CGXInventoryUnpackTask;
import com.io7m.coffeepick.gui.controller.internal.CGXRepositoryUpdateAllTask;
import com.io7m.coffeepick.gui.controller.internal.CGXRepositoryUpdateTask;
import com.io7m.coffeepick.gui.properties.Property;
import com.io7m.coffeepick.gui.properties.PropertyReadableType;
import com.io7m.coffeepick.gui.properties.PropertyType;
import com.io7m.coffeepick.repository.spi.RuntimeRepositoryType;
import com.io7m.coffeepick.runtime.RuntimeDescription;
import com.io7m.jade.api.ApplicationDirectoriesType;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static com.io7m.coffeepick.gui.controller.CGXControllerTaskStatus.TASK_CANCELLED;
import static com.io7m.coffeepick.gui.controller.CGXControllerTaskStatus.TASK_FAILED;
import static com.io7m.coffeepick.gui.controller.CGXControllerTaskStatus.TASK_RUNNING;
import static com.io7m.coffeepick.gui.controller.CGXControllerTaskStatus.TASK_SUCCEEDED;

/**
 * The default controller.
 */

public final class CGXController implements CGXControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CGXController.class);

  private final ApplicationDirectoriesType directories;
  private final CGXControllerStringsType strings;
  private final CoffeePickClientProviderType clients;
  private final ExecutorService executor;
  private final InternalAccess internal;
  private final PropertyType<Boolean> taskRunning;
  private final PropertyType<List<CGXControllerTaskType<?>>> tasks;
  private final PropertyType<List<RuntimeRepositoryType>> repositories;
  private final PropertyType<Map<String, RuntimeDescription>> catalog;
  private final PropertyType<Map<String, RuntimeDescription>> inventory;
  private final Subject<CGXControllerEventType> events;
  private final CompositeDisposable taskSubscriptions;
  private volatile CoffeePickClientType client;

  public CGXController(
    final ExecutorService inExecutor,
    final ApplicationDirectoriesType inDirectories,
    final CoffeePickClientProviderType inClients,
    final CGXControllerStringsType inStrings)
  {
    this.executor =
      Objects.requireNonNull(inExecutor, "executor");
    this.directories =
      Objects.requireNonNull(inDirectories, "directories");
    this.clients =
      Objects.requireNonNull(inClients, "clients");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");

    this.taskRunning =
      Property.of(Boolean.FALSE);
    this.events =
      BehaviorSubject.<CGXControllerEventType>create()
        .toSerialized();

    this.internal = new InternalAccess();
    this.catalog = Property.of(Map.of());
    this.inventory = Property.of(Map.of());
    this.repositories = Property.of(List.of());
    this.tasks = Property.of(List.of());
    this.taskSubscriptions = new CompositeDisposable();
    this.tasks.observable().subscribe(this::onTaskListUpdated);
  }

  private void onTaskListUpdated(
    final List<CGXControllerTaskType<?>> currentTasks)
  {
    if (currentTasks.isEmpty()) {
      this.taskRunning.setValue(Boolean.FALSE);
      return;
    }

    final var lastTask = currentTasks.get(currentTasks.size() - 1);
    final var lastTaskStatus = lastTask.status().value();
    this.taskRunning.setValue(
      Boolean.valueOf(lastTaskStatus == TASK_RUNNING));
  }

  public static CGXControllerType create(
    final ApplicationDirectoriesType directories,
    final CoffeePickClientProviderType clients)
  {
    final var strings =
      CGXControllerStrings.of(CGXControllerStrings.getResourceBundle());

    final var executor =
      Executors.newSingleThreadExecutor(
        runnable -> {
          final var thread = new Thread(runnable);
          thread.setDaemon(true);
          thread.setName(
            String.format(
              "com.io7m.coffeepick.gui.controller[%d]",
              Long.valueOf(thread.getId())
            )
          );
          return thread;
        }
      );

    final var controller =
      new CGXController(executor, directories, clients, strings);
    controller.boot();
    return controller;
  }

  private <T> CGXControllerTaskType<T> submitTask(
    final Function<CompletableFuture<T>, CGXAbstractTask<T>> taskFactory)
  {
    final var future = new CompletableFuture<T>();
    final var task = taskFactory.apply(future);

    /*
     * Subscribe to the task status in order to publish task list status
     * updates.
     */

    this.taskSubscriptions.add(
      task.status().observable()
        .subscribe(status -> {
          LOG.debug("task {} status changed", task);
          this.tasks.setMap(Function.identity());
        })
    );

    /*
     * Add the task to the queue.
     */

    this.tasks.setMap(current -> {
      final var newTasks = new ArrayList<>(current);
      newTasks.add(task);
      return newTasks;
    });

    /*
     * Execute the task!
     */

    this.executor.execute(() -> {
      try {
        task.setStatus(TASK_RUNNING);

        this.events.onNext(
          CGXControllerEventTaskStarted.builder()
            .setDescription(task.description())
            .build()
        );

        this.events.onNext(
          CGXControllerEventTaskRunning.builder()
            .setDescription(task.description())
            .setProgressMajor(OptionalDouble.empty())
            .setProgressMinor(OptionalDouble.empty())
            .build()
        );

        LOG.debug("task execute: {}", task);
        final T result = task.execute();
        task.setStatus(TASK_SUCCEEDED);

        this.events.onNext(
          CGXControllerEventTaskCompleted.builder()
            .setDescription(this.strings.taskCompleted(task.description()))
            .build()
        );

        future.complete(result);
      } catch (final CancellationException e) {
        task.setStatus(TASK_CANCELLED);

        this.events.onNext(
          CGXControllerEventTaskCancelled.builder()
            .setDescription(this.strings.taskCancelled(task.description()))
            .build()
        );
      } catch (final Throwable e) {
        LOG.error("task exception: ", e);
        future.completeExceptionally(e);
        task.setStatus(TASK_FAILED);

        this.events.onNext(
          CGXControllerEventTaskFailed.builder()
            .setDescription(this.strings.taskFailed(task.description()))
            .build()
        );
      }
    });

    return task;
  }

  private void boot()
  {
    this.submitTask(this::createBootTask)
      .future()
      .whenComplete((newClient, exception) -> this.client = newClient);
  }

  private CGXAbstractTask<CoffeePickClientType> createBootTask(
    final CompletableFuture<CoffeePickClientType> future)
  {
    return new CGXBootTask(
      this.internal,
      this.directories,
      this.clients,
      this.strings,
      future);
  }

  @Override
  public void close()
  {
    this.executor.shutdown();
  }

  @Override
  public Observable<CGXControllerEventType> events()
  {
    return this.events;
  }

  @Override
  public PropertyType<Boolean> taskRunning()
  {
    return this.taskRunning;
  }

  @Override
  public void cancel()
  {
    final List<CGXControllerTaskType<?>> tasksNow = this.tasks.value();
    if (!tasksNow.isEmpty()) {
      final var taskNow = tasksNow.get(tasksNow.size() - 1);
      taskNow.cancel();
    }
  }

  @Override
  public CGXControllerTaskType<?> inventoryDelete(
    final Set<String> ids)
  {
    return this.submitTask(
      (CompletableFuture<Void> future) ->
        new CGXInventoryDeleteTask(
          this.internal,
          this.client,
          this.strings,
          future,
          ids
        ));
  }

  @Override
  public CGXControllerTaskType<?> inventoryUnpack(
    final Set<String> ids,
    final Path path)
  {
    return this.submitTask(
      (CompletableFuture<Void> future) ->
        new CGXInventoryUnpackTask(
          this.internal,
          this.client,
          this.strings,
          future,
          ids,
          path
        ));
  }

  @Override
  public CGXControllerTaskType<?> repositoryUpdateAll()
  {
    return this.submitTask(
      (CompletableFuture<Void> future) ->
        new CGXRepositoryUpdateAllTask(
          this.internal,
          this.client,
          this.strings,
          future)
    );
  }

  @Override
  public CGXControllerTaskType<?> catalogDownload(
    final Set<String> ids)
  {
    return this.submitTask(
      (CompletableFuture<Void> future) ->
        new CGXCatalogDownloadRuntimesTask(
          this.internal,
          future,
          this.client,
          this.strings,
          ids
        )
    );
  }

  @Override
  public PropertyReadableType<Map<String, RuntimeDescription>> catalog()
  {
    return this.catalog;
  }

  @Override
  public PropertyReadableType<Map<String, RuntimeDescription>> inventory()
  {
    return this.inventory;
  }

  @Override
  public PropertyReadableType<List<RuntimeRepositoryType>> repositories()
  {
    return this.repositories;
  }

  @Override
  public PropertyReadableType<List<CGXControllerTaskType<?>>> tasks()
  {
    return this.tasks;
  }

  @Override
  public CGXControllerTaskType<?> repositoryUpdate(
    final URI uri)
  {
    return this.submitTask(
      (CompletableFuture<Void> future) ->
        new CGXRepositoryUpdateTask(
          this.internal,
          this.client,
          this.strings,
          uri,
          future)
    );
  }

  @Override
  public String toString()
  {
    return String.format(
      "[CGXController 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }

  @Override
  public CGXControllerTaskType<?> debugFail(final long time)
  {
    return this.submitTask(
      (CompletableFuture<Void> future) ->
        new CGXDebugFailTask(
          this.internal,
          future,
          time
        )
    );
  }

  private final class InternalAccess implements CGXControllerInternalType
  {
    InternalAccess()
    {

    }

    @Override
    public void setCatalog(
      final Map<String, RuntimeDescription> newRuntimes)
    {
      CGXController.this.catalog.setValue(
        Objects.requireNonNull(newRuntimes, "runtimes")
      );
    }

    @Override
    public void setRepositories(
      final List<RuntimeRepositoryType> newRepositories)
    {
      CGXController.this.repositories.setValue(
        Objects.requireNonNull(newRepositories, "repositories")
      );
    }

    @Override
    public void setInventory(
      final Map<String, RuntimeDescription> newRuntimes)
    {
      CGXController.this.inventory.setValue(
        Objects.requireNonNull(newRuntimes, "runtimes")
      );
    }

    @Override
    public void publishEvent(
      final CGXControllerEventType event)
    {
      CGXController.this.events.onNext(
        Objects.requireNonNull(event, "event")
      );
    }
  }
}
