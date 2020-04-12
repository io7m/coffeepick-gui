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

import com.io7m.coffeepick.gui.properties.PropertyReadableType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A task.
 *
 * @param <T> The type of returned values
 */

public interface CGXControllerTaskType<T>
{
  /**
   * @return The task future
   */

  CompletableFuture<T> future();

  /**
   * @return The description of the task
   */

  String description();

  /**
   * @return The current task status
   */

  PropertyReadableType<CGXControllerTaskStatus> status();

  /**
   * Cancel the task in progress.
   */

  default void cancel()
  {
    this.future().cancel(true);
  }

  /**
   * @return The exception, if the task has failed
   */

  default Optional<Throwable> exception()
  {
    try {
      final var future = this.future();
      if (future.isCompletedExceptionally()) {
        future.get();
      }
      return Optional.empty();
    } catch (final ExecutionException e) {
      return Optional.ofNullable(e.getCause());
    } catch (final Throwable e) {
      return Optional.of(e);
    }
  }
}
