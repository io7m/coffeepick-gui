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

import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskRunning;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class CGXDebugFailTask extends CGXAbstractTask<Void>
{
  private final CGXControllerInternalType controller;
  private final long seconds;

  public CGXDebugFailTask(
    final CGXControllerInternalType inController,
    final CompletableFuture<Void> inFuture,
    final long inSeconds)
  {
    super(inFuture);
    this.controller = Objects.requireNonNull(inController, "inController");
    this.seconds = inSeconds;
  }

  @Override
  public Void execute()
    throws Exception
  {
    for (long index = 0L; index < this.seconds; ++index) {
      try {
        final var progess = (double) index / (double) this.seconds;
        this.controller.publishEvent(
          CGXControllerEventTaskRunning.builder()
            .setDescription(
              String.format(
                "Failing (%d of %d)",
                Long.valueOf(index + 1L),
                Long.valueOf(this.seconds)))
            .setProgressMinor(progess)
            .build()
        );
        TimeUnit.SECONDS.sleep(1L);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    throw new IOException("Failed!");
  }

  @Override
  public String description()
  {
    return "Failing";
  }
}
