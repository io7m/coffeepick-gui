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

import com.io7m.coffeepick.gui.controller.CGXControllerType;
import com.io7m.coffeepick.gui.fx.CGXViewControllerFactoryType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public final class CGXViewControllerDebug implements CGXViewControllerType
{
  private final CGXServiceDirectoryType services;
  private final CGXControllerType controller;

  @FXML private TextField failTimeField;

  public CGXViewControllerDebug(
    final CGXViewControllerFactoryType inControllers,
    final CGXServiceDirectoryType inServices)
  {
    this.services =
      Objects.requireNonNull(inServices, "inServices");
    this.controller =
      inServices.requireService(CGXControllerType.class);
  }

  @FXML
  private void onFailSelected()
  {
    final var time =
      Math.min(
        Long.parseUnsignedLong(this.failTimeField.getText()),
        10L
      );

    this.controller.debugFail(time);
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {

  }
}
