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

package com.io7m.coffeepick.gui.controller.internal;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The controller string provider.
 */

public final class CGXControllerStrings implements CGXControllerStringsType
{
  private final ResourceBundle resourceBundle;

  private CGXControllerStrings(
    final ResourceBundle inResourceBundle)
  {
    this.resourceBundle = inResourceBundle;
  }

  /**
   * Retrieve the resource bundle for the given locale.
   *
   * @param locale The locale
   *
   * @return The resource bundle
   */

  public static ResourceBundle getResourceBundle(
    final Locale locale)
  {
    return ResourceBundle.getBundle(
      "com.io7m.coffeepick.gui.controller.internal.Strings",
      locale);
  }

  /**
   * Retrieve the resource bundle for the current locale.
   *
   * @return The resource bundle
   */

  public static ResourceBundle getResourceBundle()
  {
    return getResourceBundle(Locale.getDefault());
  }

  /**
   * Create a new string provider from the given bundle.
   *
   * @param bundle The resource bundle
   *
   * @return A string provider
   */

  public static CGXControllerStringsType of(
    final ResourceBundle bundle)
  {
    return new CGXControllerStrings(bundle);
  }

  @Override
  public ResourceBundle resourceBundle()
  {
    return this.resourceBundle;
  }

  @Override
  public String format(
    final String id,
    final Object... args)
  {
    return MessageFormat.format(this.resourceBundle.getString(id), args);
  }

  @Override
  public String toString()
  {
    return String.format(
      "[CGXControllerStrings 0x%s]",
      Integer.toUnsignedString(System.identityHashCode(this), 16)
    );
  }

  @Override
  public String taskRunning(final String description)
  {
    return this.format("controller.task.running", description);

  }

  @Override
  public String taskStarted(final String description)
  {
    return this.format("controller.task.started", description);

  }

  @Override
  public String taskCompleted(final String description)
  {
    return this.format("controller.task.completed", description);
  }

  @Override
  public String taskFailed(final String description)
  {
    return this.format("controller.task.failed", description);
  }

  @Override
  public String repositoryUpdating(final URI repository)
  {
    return this.format("controller.task.repository.updating", repository);
  }

  @Override
  public String taskCancelled(final String description)
  {
    return this.format("controller.task.cancelled", description);
  }
}
