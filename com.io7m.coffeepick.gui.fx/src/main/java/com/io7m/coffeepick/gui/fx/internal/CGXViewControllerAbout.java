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

package com.io7m.coffeepick.gui.fx.internal;

import com.io7m.coffeepick.gui.fx.CGXViewControllerFactoryType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * The view controller for the About window.
 */

public final class CGXViewControllerAbout implements CGXViewControllerType
{
  private final CGXServiceDirectoryType services;

  @FXML private Label applicationField;
  @FXML private Hyperlink linkField;

  public CGXViewControllerAbout(
    final CGXViewControllerFactoryType inControllers,
    final CGXServiceDirectoryType inServices)
  {
    this.services =
      Objects.requireNonNull(inServices, "inServices");
  }

  private static String applicationName()
  {
    var appName = "com.io7m.coffeepick.gui";
    var appVersion = "0.0.0";
    final var pack = CGXViewControllerAbout.class.getPackage();
    if (pack != null) {
      final var title = pack.getImplementationTitle();
      if (title != null) {
        appName = title;
      }
      final var version = pack.getImplementationVersion();
      if (version != null) {
        appVersion = version;
      }
    }
    return String.format("%s %s", appName, appVersion);
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    this.applicationField.setText(applicationName());
  }
}
