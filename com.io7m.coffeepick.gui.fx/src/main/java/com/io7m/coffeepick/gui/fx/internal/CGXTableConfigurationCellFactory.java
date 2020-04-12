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

import com.io7m.coffeepick.runtime.RuntimeDescription;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

/**
 * A factory for configuration cells.
 */

public final class CGXTableConfigurationCellFactory
  implements Callback<
  TableColumn<RuntimeDescription, RuntimeDescription>,
  TableCell<RuntimeDescription, RuntimeDescription>>
{
  private final Tooltip tooltipJDK;
  private final Tooltip tooltipJRE;

  /**
   * Construct a cell factory.
   *
   * @param strings String resources
   */

  public CGXTableConfigurationCellFactory(
    final CGXUIStringsType strings)
  {
    this.tooltipJDK =
      new Tooltip(strings.format("ui.tooltip.runtimeIsJDK"));
    this.tooltipJRE =
      new Tooltip(strings.format("ui.tooltip.runtimeIsJRE"));
  }

  @Override
  public TableCell<RuntimeDescription, RuntimeDescription> call(
    final TableColumn<RuntimeDescription, RuntimeDescription> column)
  {
    return new CGXTableConfigurationCell();
  }

  /**
   * The cell.
   */

  private final class CGXTableConfigurationCell
    extends TableCell<RuntimeDescription, RuntimeDescription>
  {
    CGXTableConfigurationCell()
    {

    }

    @Override
    protected void updateItem(
      final RuntimeDescription item,
      final boolean empty)
    {
      super.updateItem(item, empty);

      this.setGraphic(null);
      this.setText(null);
      this.setTooltip(null);

      if (empty || item == null) {
        return;
      }

      switch (item.configuration()) {
        case JRE: {
          this.setTooltip(CGXTableConfigurationCellFactory.this.tooltipJRE);
          this.setGraphic(CGXImages.iconOf("jre16.png"));
          break;
        }
        case JDK: {
          this.setTooltip(CGXTableConfigurationCellFactory.this.tooltipJDK);
          this.setGraphic(CGXImages.iconOf("jdk16.png"));
          break;
        }
      }
    }
  }
}
