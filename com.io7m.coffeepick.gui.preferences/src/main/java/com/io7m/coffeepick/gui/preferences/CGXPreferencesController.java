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

package com.io7m.coffeepick.gui.preferences;

import com.io7m.coffeepick.gui.properties.Property;
import com.io7m.coffeepick.gui.properties.PropertyReadableType;
import com.io7m.coffeepick.gui.properties.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

public final class CGXPreferencesController
  implements CGXPreferencesControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CGXPreferencesController.class);

  private final PropertyType<CGXPreferences> preferences;
  private final Path file;
  private final Path fileTmp;
  private final ExecutorService executor;

  private CGXPreferencesController(
    final Path inFile,
    final Path inFileTmp,
    final ExecutorService inExecutor)
  {
    this.file =
      Objects.requireNonNull(inFile, "file");
    this.fileTmp =
      Objects.requireNonNull(inFileTmp, "fileTmp");
    this.executor =
      Objects.requireNonNull(inExecutor, "executor");
    this.preferences =
      Property.of(
        defaultPreferences()
      );
  }

  private static CGXPreferences defaultPreferences()
  {
    final CGXPreferencesDebug debug =
      CGXPreferencesDebug.builder()
        .setDebugEnabled(false)
        .build();

    return CGXPreferences.builder()
      .setDebug(debug)
      .build();
  }

  public static CGXPreferencesControllerType create(
    final Path file,
    final Path fileTmp)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(fileTmp, "fileTmp");

    final var executor =
      Executors.newSingleThreadExecutor(
        runnable -> {
          final var thread = new Thread(runnable);
          thread.setDaemon(true);
          thread.setName(
            String.format(
              "com.io7m.coffeepick.gui.preferences[%d]",
              Long.valueOf(thread.getId())
            )
          );
          return thread;
        }
      );

    final var controller =
      new CGXPreferencesController(file, fileTmp, executor);
    controller.load();
    controller.update(Function.identity());
    return controller;
  }

  private void load()
  {
    this.executor.execute(() -> {
      try (var stream = Files.newInputStream(this.file)) {
        this.preferences.setValue(CGXPreferencesXML.loadFromXML(stream));
      } catch (final NoSuchFileException e) {
        // No problem
      } catch (final IOException e) {
        LOG.error("error loading preferences: ", e);
      }
    });
  }

  @Override
  public PropertyReadableType<CGXPreferences> preferences()
  {
    return this.preferences;
  }

  @Override
  public CompletableFuture<?> update(
    final Function<CGXPreferences, CGXPreferences> updater)
  {
    this.preferences.setMap(Objects.requireNonNull(updater, "updater"));

    final var future = new CompletableFuture<>();
    this.executor.execute(() -> {
      try {
        Files.createDirectories(this.file.getParent());
      } catch (final IOException e) {
        LOG.error("could not create preferences directory: ", e);
      }

      LOG.debug("storing preferences: {}", this.file);
      try (var stream =
             Files.newOutputStream(this.fileTmp, CREATE_NEW, WRITE)) {
        CGXPreferencesXML.serializeToXML(stream, this.preferences.value());
        Files.move(this.fileTmp, this.file, REPLACE_EXISTING, ATOMIC_MOVE);
      } catch (final IOException e) {
        LOG.error("could not update preferences: ", e);
      }
    });
    return future;
  }

  @Override
  public void close()
  {
    this.executor.shutdown();
  }
}
