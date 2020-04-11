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

package com.io7m.coffeepick.gui.fx;

import com.io7m.coffeepick.gui.fx.internal.CGXViewControllerAbout;
import com.io7m.coffeepick.gui.fx.internal.CGXViewControllerMain;
import com.io7m.coffeepick.gui.fx.internal.CGXViewControllerRepositories;
import com.io7m.coffeepick.gui.fx.internal.CGXViewControllerRuntime;
import com.io7m.coffeepick.gui.fx.internal.CGXViewControllerSearch;
import com.io7m.coffeepick.gui.fx.internal.CGXViewControllerTasks;
import com.io7m.coffeepick.gui.fx.internal.CGXViewControllerType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import javafx.fxml.FXMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class CGXViewControllers implements CGXViewControllerFactoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CGXViewControllers.class);

  private final CGXServiceDirectoryType services;
  private final Map<String, Supplier<CGXViewControllerType>> controllers;

  private CGXViewControllers(
    final CGXServiceDirectoryType inServices)
  {
    this.services = Objects.requireNonNull(inServices, "services");
    this.controllers = makeControllers(this, inServices);
  }

  public static CGXViewControllerFactoryType create(
    final CGXServiceDirectoryType inServices)
  {
    return new CGXViewControllers(inServices);
  }

  private static Map<String, Supplier<CGXViewControllerType>> makeControllers(
    final CGXViewControllerFactoryType factory,
    final CGXServiceDirectoryType inServices)
  {
    return Map.ofEntries(
      Map.entry(
        CGXViewControllerMain.class.getCanonicalName(),
        () -> new CGXViewControllerMain(factory, inServices)
      ),
      Map.entry(
        CGXViewControllerSearch.class.getCanonicalName(),
        () -> new CGXViewControllerSearch(factory, inServices)
      ),
      Map.entry(
        CGXViewControllerTasks.class.getCanonicalName(),
        () -> new CGXViewControllerTasks(factory, inServices)
      ),
      Map.entry(
        CGXViewControllerRuntime.class.getCanonicalName(),
        () -> new CGXViewControllerRuntime(factory, inServices)
      ),
      Map.entry(
        CGXViewControllerRepositories.class.getCanonicalName(),
        () -> new CGXViewControllerRepositories(factory, inServices)
      ),
      Map.entry(
        CGXViewControllerAbout.class.getCanonicalName(),
        () -> new CGXViewControllerAbout(factory, inServices)
      )
    );
  }

  @Override
  public Object createController(
    final Class<?> clazz)
  {
    Objects.requireNonNull(clazz, "clazz");

    final var name = clazz.getCanonicalName();
    LOG.debug("create: {}", name);

    final var creator = this.controllers.get(name);
    if (creator == null) {
      throw new UnsupportedOperationException(
        String.format("No view controller registered: %s", name)
      );
    }

    return creator.get();
  }

  @Override
  public FXMLLoader createMainLoader()
  {
    return CGXViewControllerMain.createLoader(this);
  }
}
