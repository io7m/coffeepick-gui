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

import com.io7m.coffeepick.api.CoffeePickCatalogEventRuntimeDownloading;
import com.io7m.coffeepick.api.CoffeePickClientType;
import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskRunning;
import org.apache.commons.io.FileUtils;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class CGXCatalogDownloadRuntimesTask extends CGXAbstractTask<Void>
{
  private final CGXControllerInternalType controller;
  private final CGXControllerStringsType strings;
  private final CompletableFuture<Void> future;
  private final CoffeePickClientType client;
  private final Set<String> ids;
  private volatile int runtimeIndex;

  public CGXCatalogDownloadRuntimesTask(
    final CGXControllerInternalType inController,
    final CompletableFuture<Void> inFuture,
    final CoffeePickClientType inClient,
    final CGXControllerStringsType inStrings,
    final Set<String> inIds)
  {
    super(inFuture);
    this.controller =
      Objects.requireNonNull(inController, "inController");
    this.future =
      Objects.requireNonNull(inFuture, "future");
    this.client =
      Objects.requireNonNull(inClient, "client");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.ids =
      Objects.requireNonNull(inIds, "id");
  }

  @Override
  public Void execute()
    throws Exception
  {
    final var subscription =
      this.client.events()
        .ofType(CoffeePickCatalogEventRuntimeDownloading.class)
        .subscribe(this::onDownloadEvent);

    try {
      this.runtimeIndex = 0;
      for (final var id : this.ids) {
        final var downloadFuture = this.client.catalogDownload(id);
        this.future.whenComplete((path, throwable) -> {
          if (this.future.isCancelled()) {
            downloadFuture.cancel(true);
          }
        });
        downloadFuture.get();
        this.controller.setInventory(this.client.inventorySearchAll().get());
        ++this.runtimeIndex;
      }
      return null;
    } finally {
      subscription.dispose();
    }
  }

  private void onDownloadEvent(
    final CoffeePickCatalogEventRuntimeDownloading event)
  {
    final var majorIndex =
      (double) this.runtimeIndex / (double) this.ids.size();

    this.controller.publishEvent(
      CGXControllerEventTaskRunning.builder()
        .setDescription(this.progressMessage(event))
        .setProgressMajor(majorIndex)
        .setProgressMinor(event.progress())
        .build()
    );
  }

  private String progressMessage(
    final CoffeePickCatalogEventRuntimeDownloading event)
  {
    final var shortId =
      event.id().substring(0, 7);
    final var received =
      FileUtils.byteCountToDisplaySize(event.received());
    final var expected =
      FileUtils.byteCountToDisplaySize(event.expected());
    final var perSecond =
      FileUtils.byteCountToDisplaySize((long) event.octetsPerSecond()) + "/s";
    final var secondsRemaining =
      (double) (event.expected() - event.received()) / event.octetsPerSecond();
    final var timeRemaining =
      Duration.ofSeconds((long) secondsRemaining);
    final var timeRemainingString =
      String.format(
        "%02d:%02d:%02d",
        Integer.valueOf(timeRemaining.toHoursPart()),
        Integer.valueOf(timeRemaining.toMinutesPart()),
        Integer.valueOf(timeRemaining.toSecondsPart())
      );

    return this.strings.format(
      "controller.task.catalogDownloadStatus",
      Integer.valueOf(this.runtimeIndex + 1),
      Integer.valueOf(this.ids.size()),
      shortId,
      received,
      expected,
      perSecond,
      timeRemainingString
    );
  }

  @Override
  public String description()
  {
    return this.strings.format(
      "controller.task.catalogDownload",
      Integer.valueOf(this.ids.size())
    );
  }

  @Override
  public String toString()
  {
    return String.format(
      "[CGXCatalogDownloadRuntimeTask 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
