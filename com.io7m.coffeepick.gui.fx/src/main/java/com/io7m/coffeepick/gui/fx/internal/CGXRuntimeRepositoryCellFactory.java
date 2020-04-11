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

import com.io7m.coffeepick.repository.spi.RuntimeRepositoryType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public final class CGXRuntimeRepositoryCellFactory
  implements Callback<
  ListView<RuntimeRepositoryType>,
  ListCell<RuntimeRepositoryType>>
{
  public CGXRuntimeRepositoryCellFactory()
  {

  }

  @Override
  public ListCell<RuntimeRepositoryType> call(
    final ListView<RuntimeRepositoryType> param)
  {
    return new RuntimeRepositoryTypeListCell();
  }

  private static final class RuntimeRepositoryTypeListCell
    extends ListCell<RuntimeRepositoryType>
  {
    RuntimeRepositoryTypeListCell()
    {

    }

    @Override
    protected void updateItem(
      final RuntimeRepositoryType item,
      final boolean empty)
    {
      super.updateItem(item, empty);

      this.setGraphic(null);
      this.setText(null);
      this.setTooltip(null);

      if (empty || item == null) {
        return;
      }

      this.setText(
        String.format(
          "%s - %s",
          item.provider().name(),
          item.description().branding().title())
      );
    }
  }
}
