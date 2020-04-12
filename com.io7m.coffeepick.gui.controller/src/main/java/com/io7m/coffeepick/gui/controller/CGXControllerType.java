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

/**
 * The main application controller.
 */

public interface CGXControllerType
  extends Closeable, CGXServiceType, CGXControllerDebugType
{
  /**
   * @return A read-only view of the current catalog
   */

  PropertyReadableType<Map<String, RuntimeDescription>> catalog();

  /**
   * @return A read-only view of the current inventory
   */

  PropertyReadableType<Map<String, RuntimeDescription>> inventory();

  /**
   * @return A read-only view of the current repositories
   */

  PropertyReadableType<List<RuntimeRepositoryType>> repositories();

  /**
   * @return A read-only view of the current tasks
   */

  PropertyReadableType<List<CGXControllerTaskType<?>>> tasks();

  /**
   * Update the repository with the given URI.
   *
   * @param uri The URI
   *
   * @return The task in progress
   */

  CGXControllerTaskType<?> repositoryUpdate(URI uri);

  /**
   * Update all repositories.
   *
   * @return The task in progress
   */

  CGXControllerTaskType<?> repositoryUpdateAll();

  /**
   * Download the given runtimes.
   *
   * @param ids The runtime IDs
   *
   * @return The task in progress
   */

  CGXControllerTaskType<?> catalogDownload(
    Set<String> ids
  );

  /**
   * Download the given runtime.
   *
   * @param id The runtime ID
   *
   * @return The task in progress
   */

  default CGXControllerTaskType<?> catalogDownload(
    final String id)
  {
    return this.catalogDownload(Set.of(id));
  }

  /**
   * @return An observable source of controller events
   */

  Observable<CGXControllerEventType> events();

  /**
   * @return A boolean property that indicates whether or not a task is running
   */

  PropertyReadableType<Boolean> taskRunning();

  /**
   * Cancel the currently running task
   */

  void cancel();

  /**
   * Delete the given runtimes from the inventory.
   *
   * @param ids The runtimes
   *
   * @return The task in progress
   */

  CGXControllerTaskType<?> inventoryDelete(
    Set<String> ids
  );

  /**
   * Unpack the given runtimes from the inventory.
   *
   * @param ids  The runtimes
   * @param path The directory that will contain the runtimes
   *
   * @return The task in progress
   */

  CGXControllerTaskType<?> inventoryUnpack(
    Set<String> ids,
    Path path
  );
}
