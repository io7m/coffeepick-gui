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

package com.io7m.coffeepick.gui.main;

import com.io7m.coffeepick.client.vanilla.CoffeePickClients;
import com.io7m.coffeepick.gui.controller.CGXController;
import com.io7m.coffeepick.gui.controller.CGXControllerType;
import com.io7m.coffeepick.gui.directories.api.CGXDirectoriesType;
import com.io7m.coffeepick.gui.filechooser.api.CGXFileChoosersType;
import com.io7m.coffeepick.gui.preferences.CGXPreferencesController;
import com.io7m.coffeepick.gui.preferences.CGXPreferencesControllerType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectory;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import com.io7m.jade.api.ApplicationDirectories;
import com.io7m.jade.api.ApplicationDirectoryConfiguration;

public final class MainServices
{
  private MainServices()
  {

  }

  public static CGXServiceDirectoryType create()
  {
    final var directories =
      ApplicationDirectories.get(
        ApplicationDirectoryConfiguration.builder()
          .setApplicationName("com.io7m.coffeepick")
          .setOverridePropertyName("com.io7m.coffeepick.baseDirectory")
          .setPortablePropertyName("com.io7m.coffeepick.portable")
          .build()
      );

    final var services = new CGXServiceDirectory();
    final var preferences =
      CGXPreferencesController.create(
        directories.configurationDirectory().resolve("preferences.xml"),
        directories.configurationDirectory().resolve("preferences.xml.tmp")
      );
    services.register(CGXPreferencesControllerType.class, preferences);
    services.register(CGXDirectoriesType.class, () -> directories);

    final var clients = CoffeePickClients.create();
    final var controller = CGXController.create(directories, clients);
    services.register(CGXControllerType.class, controller);
    final var choosers = new MainFileChoosers();
    services.register(CGXFileChoosersType.class, choosers);
    return services;
  }
}
