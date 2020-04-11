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

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class CGXInventoryDeleteTask extends CGXAbstractTask<Void>
{
  private final CGXControllerInternalType controller;
  private final CoffeePickClientType client;
  private final CGXControllerStringsType strings;
  private final CompletableFuture<Void> future;
  private final Set<String> ids;
  private int runtimeIndex;

  public CGXInventoryDeleteTask(
    final CGXControllerInternalType inController,
    final CoffeePickClientType inClient,
    final CGXControllerStringsType inStrings,
    final CompletableFuture<Void> inFuture,
    final Set<String> inIds)
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
      Objects.requireNonNull(inIds, "ids");
  }

  @Override
  public Void execute()
    throws Exception
  {
    this.runtimeIndex = 0;
    for (final var id : this.ids) {
      this.client.inventoryDelete(id).get();
      this.controller.setInventory(this.client.inventorySearchAll().get());
    }
    return null;
  }

  @Override
  public String description()
  {
    return this.strings.format(
      "controller.task.inventoryDelete",
      Integer.valueOf(this.ids.size())
    );
  }

  @Override
  public String toString()
  {
    return String.format(
      "[CGXInventoryDeleteTask 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
