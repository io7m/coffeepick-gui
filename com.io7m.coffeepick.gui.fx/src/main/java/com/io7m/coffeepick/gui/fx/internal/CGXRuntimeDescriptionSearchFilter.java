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

import com.io7m.coffeepick.api.CoffeePickSearch;
import com.io7m.coffeepick.api.CoffeePickSearches;
import com.io7m.coffeepick.runtime.RuntimeDescription;

import java.util.Objects;

/**
 * A runtime filter based on {@link CoffeePickSearch} parameters.
 */

public final class CGXRuntimeDescriptionSearchFilter
  implements CGXRuntimeDescriptionFilterType
{
  private final CoffeePickSearch search;

  private CGXRuntimeDescriptionSearchFilter(
    final CoffeePickSearch inSearch)
  {
    this.search = Objects.requireNonNull(inSearch, "search");
  }

  /**
   * Create a new filter based on the given search parameters.
   *
   * @param inSearch The search parameters
   *
   * @return A new filter
   */

  public static CGXRuntimeDescriptionFilterType of(
    final CoffeePickSearch inSearch)
  {
    return new CGXRuntimeDescriptionSearchFilter(inSearch);
  }

  @Override
  public boolean isAllowed(final RuntimeDescription runtime)
  {
    return CoffeePickSearches.matchesInexact(runtime, this.search);
  }
}
