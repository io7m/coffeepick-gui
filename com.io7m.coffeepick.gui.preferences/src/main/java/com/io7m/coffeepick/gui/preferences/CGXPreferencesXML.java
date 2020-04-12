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

package com.io7m.coffeepick.gui.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * Functions to serialize/deserialize preferences.
 */

public final class CGXPreferencesXML
{
  private CGXPreferencesXML()
  {

  }

  /**
   * Load preferences from the given stream.
   *
   * @param stream The stream
   *
   * @return Loaded preferences
   *
   * @throws IOException On errors
   */

  public static CGXPreferences loadFromXML(
    final InputStream stream)
    throws IOException
  {
    Objects.requireNonNull(stream, "stream");

    final var properties = new Properties();
    properties.loadFromXML(stream);
    final CGXPreferencesDebug debug = deserializeDebug(properties);
    return CGXPreferences.builder()
      .setDebug(debug)
      .build();
  }

  private static CGXPreferencesDebug deserializeDebug(
    final Properties properties)
  {
    final var builder = CGXPreferencesDebug.builder();
    builder.setDebugEnabled(
      Boolean.parseBoolean(properties.getProperty(
        "debug.enabled",
        "false")
      )
    );
    return builder.build();
  }

  /**
   * Serialize preferences to the given stream.
   *
   * @param stream The stream
   * @param values The preferences
   *
   * @throws IOException On errors
   */

  public static void serializeToXML(
    final OutputStream stream,
    final CGXPreferences values)
    throws IOException
  {
    Objects.requireNonNull(stream, "stream");
    Objects.requireNonNull(values, "values");

    final var properties = new Properties();
    serializeDebug(properties, values.debug());
    properties.storeToXML(stream, "", StandardCharsets.UTF_8);
  }

  private static void serializeDebug(
    final Properties properties,
    final CGXPreferencesDebug debug)
  {
    properties.setProperty(
      "debug.enabled",
      String.valueOf(debug.isDebugEnabled()));
  }
}
