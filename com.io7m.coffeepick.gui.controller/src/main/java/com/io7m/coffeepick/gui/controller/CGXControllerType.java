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

import com.io7m.coffeepick.api.CoffeePickInventoryEventType;
import com.io7m.coffeepick.gui.properties.PropertyReadableType;
import com.io7m.coffeepick.gui.services.api.CGXServiceType;
import com.io7m.coffeepick.repository.spi.RuntimeRepositoryType;
import com.io7m.coffeepick.runtime.RuntimeDescription;
import io.reactivex.rxjava3.core.Observable;

import java.io.Closeable;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CGXControllerType extends Closeable, CGXServiceType
{
  PropertyReadableType<Map<String, RuntimeDescription>> catalog();

  PropertyReadableType<Map<String, RuntimeDescription>> inventory();

  PropertyReadableType<List<RuntimeRepositoryType>> repositories();

  PropertyReadableType<List<CGXControllerTaskType>> tasks();

  CompletableFuture<?> repositoryUpdate(URI uri);

  CompletableFuture<?> repositoryUpdateAll();

  CompletableFuture<?> catalogDownload(
    Set<String> id
  );

  default CompletableFuture<?> catalogDownload(
    final String id)
  {
    return this.catalogDownload(Set.of(id));
  }

  Observable<CGXControllerEventType> events();

  Observable<CoffeePickInventoryEventType> inventoryEvents();

  PropertyReadableType<Boolean> taskRunning();

  void cancel();

  CompletableFuture<?> inventoryDelete(
    Set<String> ids
  );

  CompletableFuture<?> inventoryUnpack(
    Set<String> ids,
    Path path
  );
}
