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

package com.io7m.coffeepick.gui.controller.internal;

import com.io7m.coffeepick.api.CoffeePickCatalogEventRepositoryUpdate;
import com.io7m.coffeepick.api.CoffeePickCatalogEventRepositoryUpdateType;
import com.io7m.coffeepick.api.CoffeePickClientType;
import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskRunning;
import com.io7m.coffeepick.repository.spi.RuntimeRepositoryEventUpdateFailedType;
import com.io7m.coffeepick.repository.spi.RuntimeRepositoryEventUpdateFinishedType;
import com.io7m.coffeepick.repository.spi.RuntimeRepositoryEventUpdateRunningType;
import com.io7m.coffeepick.repository.spi.RuntimeRepositoryEventUpdateStartedType;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class CGXRepositoryUpdateTask extends CGXAbstractTask<Void>
{
  private final CGXControllerInternalType controller;
  private final CoffeePickClientType client;
  private final CGXControllerStringsType strings;
  private final URI repositoryURI;
  private final CompletableFuture<Void> future;

  public CGXRepositoryUpdateTask(
    final CGXControllerInternalType inController,
    final CoffeePickClientType inClient,
    final CGXControllerStringsType inStrings,
    final URI inRepositoryURI,
    final CompletableFuture<Void> inFuture)
  {
    super(inFuture);
    this.controller =
      Objects.requireNonNull(inController, "controller");
    this.client =
      Objects.requireNonNull(inClient, "client");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.repositoryURI =
      Objects.requireNonNull(inRepositoryURI, "repositoryURI");
    this.future =
      Objects.requireNonNull(inFuture, "inFuture");
  }

  @Override
  public Void execute()
    throws Exception
  {
    final var subscription =
      this.client.events()
        .ofType(CoffeePickCatalogEventRepositoryUpdate.class)
        .subscribe(this::onRepositoryUpdateEvent);

    try {
      this.client.repositoryUpdate(this.repositoryURI).get();
      this.controller.setCatalog(this.client.catalogSearchAll().get());
      return null;
    } finally {
      subscription.dispose();
    }
  }

  private void onRepositoryUpdateEvent(
    final CoffeePickCatalogEventRepositoryUpdateType event)
  {
    final var reposEvent = event.event();
    switch (reposEvent.kind()) {
      case STARTED: {
        final var started =
          (RuntimeRepositoryEventUpdateStartedType) reposEvent;

        this.controller.publishEvent(
          CGXControllerEventTaskRunning.builder()
            .setDescription(this.strings.repositoryUpdating(started.repository()))
            .build()
        );
        break;
      }

      case RUNNING: {
        final var running =
          (RuntimeRepositoryEventUpdateRunningType) reposEvent;

        this.controller.publishEvent(
          CGXControllerEventTaskRunning.builder()
            .setDescription(this.strings.repositoryUpdating(running.repository()))
            .setProgressMinor(running.progress())
            .build()
        );
        break;
      }

      case FAILED: {
        final var failed =
          (RuntimeRepositoryEventUpdateFailedType) reposEvent;

        this.controller.publishEvent(
          CGXControllerEventTaskRunning.builder()
            .setDescription(this.strings.repositoryUpdating(failed.repository()))
            .build()
        );
        break;
      }

      case FINISHED: {
        final var finished =
          (RuntimeRepositoryEventUpdateFinishedType) reposEvent;

        this.controller.publishEvent(
          CGXControllerEventTaskRunning.builder()
            .setDescription(this.strings.repositoryUpdating(finished.repository()))
            .build()
        );
        break;
      }
    }
  }

  @Override
  public String description()
  {
    return this.strings.format(
      "controller.task.repository.updating",
      this.repositoryURI
    );
  }

  @Override
  public String toString()
  {
    return String.format(
      "[CGXRepositoryUpdateTask 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
