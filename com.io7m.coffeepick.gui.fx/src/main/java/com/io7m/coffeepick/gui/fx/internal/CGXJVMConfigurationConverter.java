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

import com.io7m.coffeepick.runtime.RuntimeConfiguration;
import javafx.util.StringConverter;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * A string converter for JVM configuration names.
 */

public final class CGXJVMConfigurationConverter
  extends StringConverter<Optional<RuntimeConfiguration>>
{
  private final String unspecified;

  /**
   * Construct a converter.
   *
   * @param inStrings String resources
   */

  public CGXJVMConfigurationConverter(
    final CGXUIStringsType inStrings)
  {
    Objects.requireNonNull(inStrings, "strings");
    this.unspecified =
      inStrings.format("ui.table.configuration.unspecified");
  }

  @Override
  public String toString(
    final Optional<RuntimeConfiguration> object)
  {
    if (object.isEmpty()) {
      return this.unspecified;
    }

    switch (object.get()) {
      case JDK:
        return "JDK";
      case JRE:
        return "JRE";
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<RuntimeConfiguration> fromString(
    final String string)
  {
    switch (string.toUpperCase(Locale.ROOT)) {
      case "JDK":
        return Optional.of(RuntimeConfiguration.JDK);
      case "JRE":
        return Optional.of(RuntimeConfiguration.JRE);
    }
    return Optional.empty();
  }
}
