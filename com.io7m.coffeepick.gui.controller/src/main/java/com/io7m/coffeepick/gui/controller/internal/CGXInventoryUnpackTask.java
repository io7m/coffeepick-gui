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

import com.io7m.coffeepick.api.CoffeePickClientType;
import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskRunning;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.io7m.coffeepick.api.CoffeePickInventoryType.UnpackOption.STRIP_LEADING_DIRECTORY;
import static com.io7m.coffeepick.api.CoffeePickInventoryType.UnpackOption.STRIP_NON_OWNER_WRITABLE;

public final class CGXInventoryUnpackTask extends CGXAbstractTask<Void>
{
  private final CGXControllerInternalType controller;
  private final CoffeePickClientType client;
  private final CGXControllerStringsType strings;
  private final CompletableFuture<Void> future;
  private final Set<String> ids;
  private final Path path;
  private volatile int runtimeIndex;

  public CGXInventoryUnpackTask(
    final CGXControllerInternalType inController,
    final CoffeePickClientType inClient,
    final CGXControllerStringsType inStrings,
    final CompletableFuture<Void> inFuture,
    final Set<String> inIds,
    final Path inPath)
  {
    super(inFuture);
    this.controller =
      Objects.requireNonNull(inController, "controller");
    this.client =
      Objects.requireNonNull(inClient, "client");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.future =
      Objects.requireNonNull(inFuture, "future");
    this.ids =
      Objects.requireNonNull(inIds, "inIds");
    this.path =
      Objects.requireNonNull(inPath, "inPath");
  }

  @Override
  public Void execute()
    throws Exception
  {
    final var unpackOptions =
      Set.of(STRIP_NON_OWNER_WRITABLE, STRIP_LEADING_DIRECTORY);

    this.runtimeIndex = 0;
    for (final var id : this.ids) {
      final var outputPath =
        this.path.resolve(id);
      final var progressMajor =
        (double) this.runtimeIndex / (double) this.ids.size();

      this.controller.publishEvent(
        CGXControllerEventTaskRunning.builder()
          .setDescription(this.descriptionNow())
          .setProgressMajor(progressMajor)
          .build()
      );

      this.client.inventoryUnpack(
        id,
        outputPath,
        unpackOptions
      ).get();
      ++this.runtimeIndex;
    }
    return null;
  }

  private String descriptionNow()
  {
    return this.strings.format(
      "controller.task.inventoryUnpackStatus",
      Integer.valueOf(this.runtimeIndex + 1),
      Integer.valueOf(this.ids.size())
    );
  }

  @Override
  public String description()
  {
    return this.strings.format(
      "controller.task.inventoryUnpack",
      Integer.valueOf(this.ids.size()),
      this.path
    );
  }

  @Override
  public String toString()
  {
    return String.format(
      "[CGXInventoryUnpackTask 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
