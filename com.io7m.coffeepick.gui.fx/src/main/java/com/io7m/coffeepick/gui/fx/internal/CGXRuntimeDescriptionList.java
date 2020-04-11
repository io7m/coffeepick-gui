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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.Objects;

/**
 * A filtered runtime description list.
 */

public final class CGXRuntimeDescriptionList
{
  private final ObservableList<RuntimeDescription> items;
  private final FilteredList<RuntimeDescription> filtered;
  private volatile CGXRuntimeDescriptionFilterType filter;

  /**
   * Construct a filtered runtime description list.
   *
   * @param inFilter The initial filter
   */

  public CGXRuntimeDescriptionList(
    final CGXRuntimeDescriptionFilterType inFilter)
  {
    this.filter =
      Objects.requireNonNull(inFilter, "inFilter");
    this.items =
      FXCollections.observableArrayList();
    this.filtered =
      this.items.filtered(this::isItemVisible);
  }

  private boolean isItemVisible(
    final RuntimeDescription item)
  {
    return this.filter.isAllowed(item);
  }

  public void setItems(
    final List<RuntimeDescription> newItems)
  {
    this.items.setAll(newItems);
  }

  public void setFilter(
    final CGXRuntimeDescriptionFilterType newFilter)
  {
    this.filter = Objects.requireNonNull(newFilter, "filter");
    this.items.setAll(List.copyOf(this.items));
  }

  public ObservableList<RuntimeDescription> items()
  {
    return this.filtered;
  }
}
