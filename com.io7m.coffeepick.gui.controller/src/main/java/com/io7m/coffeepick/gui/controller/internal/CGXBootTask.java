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

import com.io7m.coffeepick.api.CoffeePickClientProviderType;
import com.io7m.coffeepick.api.CoffeePickClientType;
import com.io7m.jade.api.ApplicationDirectoriesType;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The task that boots the CoffeePick client.
 */

public final class CGXBootTask extends CGXAbstractTask<CoffeePickClientType>
{
  private final CGXControllerInternalType controller;
  private final ApplicationDirectoriesType applicationDirectories;
  private final CoffeePickClientProviderType clients;
  private final CGXControllerStringsType strings;
  private final CompletableFuture<CoffeePickClientType> future;

  /**
   * Construct a task.
   *
   * @param inController             The controller
   * @param inApplicationDirectories The application directories
   * @param inClients                The clients
   * @param inStrings                The string resources
   * @param inFuture                 The task future
   */

  public CGXBootTask(
    final CGXControllerInternalType inController,
    final ApplicationDirectoriesType inApplicationDirectories,
    final CoffeePickClientProviderType inClients,
    final CGXControllerStringsType inStrings,
    final CompletableFuture<CoffeePickClientType> inFuture)
  {
    super(inFuture);
    this.controller =
      Objects.requireNonNull(inController, "inController");
    this.applicationDirectories =
      Objects.requireNonNull(
        inApplicationDirectories,
        "applicationDirectories");
    this.clients =
      Objects.requireNonNull(inClients, "clients");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.future =
      Objects.requireNonNull(inFuture, "inFuture");
  }

  @Override
  public CoffeePickClientType execute()
    throws Exception
  {
    final var client =
      this.clients.newClient(this.applicationDirectories.dataDirectory());
    this.controller.setInventory(client.inventorySearchAll().get());
    this.controller.setCatalog(client.catalogSearchAll().get());
    this.controller.setRepositories(client.repositoryList().get());
    return client;
  }

  @Override
  public String description()
  {
    return this.strings.format("controller.task.boot");
  }

  @Override
  public String toString()
  {
    return String.format(
      "[CGXBootTask 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
