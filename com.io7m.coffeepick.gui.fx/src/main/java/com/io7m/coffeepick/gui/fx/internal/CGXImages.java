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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Access to application image resources.
 */

public final class CGXImages
{
  private CGXImages()
  {

  }

  /**
   * Create a new image with the given name.
   *
   * @param name The name of the resource
   *
   * @return An image view
   */

  public static ImageView iconOf(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    try {
      final var path =
        String.format("/com/io7m/coffeepick/gui/fx/icons/%s", name);
      final var url =
        CGXTableConfigurationCellFactory.class.getResource(path);
      if (url == null) {
        throw new FileNotFoundException(name);
      }
      return new ImageView(url.toExternalForm());
    } catch (final FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Create a new image with the given name.
   *
   * @param name The name of the resource
   *
   * @return An image
   */

  public static Image iconImageOf(
    final String name)
  {
    return iconOf(name).getImage();
  }
}
