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

import com.io7m.coffeepick.gui.directories.api.CGXDirectoriesType;
import com.io7m.coffeepick.gui.fx.CGXViewControllerFactoryType;
import com.io7m.coffeepick.gui.preferences.CGXPreferences;
import com.io7m.coffeepick.gui.preferences.CGXPreferencesControllerType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class CGXViewControllerPreferences implements CGXViewControllerType
{
  private final CGXServiceDirectoryType services;
  private final CGXPreferencesControllerType preferences;
  private final CGXUIStringsType strings;
  private final CGXDirectoriesType directories;
  private final CompositeDisposable subscriptions;

  @FXML private CheckBox debugEnabled;
  @FXML private Pane debugPreferences;
  @FXML private Pane pathPreferences;
  @FXML private ComboBox<String> sectionChooser;
  @FXML private TextField pathConfigField;
  @FXML private TextField pathCatalogField;
  @FXML private TextField pathInventoryField;

  private Map<String, Pane> preferencesSections;
  private ObservableList<String> preferenceSectionNames;

  public CGXViewControllerPreferences(
    final CGXViewControllerFactoryType inControllers,
    final CGXServiceDirectoryType inServices)
  {
    this.services =
      Objects.requireNonNull(inServices, "inServices");
    this.preferences =
      inServices.requireService(CGXPreferencesControllerType.class);
    this.directories =
      inServices.requireService(CGXDirectoriesType.class);
    this.strings =
      CGXUIStrings.of(CGXUIStrings.getResourceBundle());
    this.subscriptions =
      new CompositeDisposable();
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    this.initializeSectionChooser();

    this.subscriptions.add(
      this.preferences.preferences()
        .observable()
        .subscribe(this::onPreferencesChanged)
    );

    final var directoryValues = this.directories.directories();
    this.pathCatalogField.setText(
      directoryValues.dataDirectory()
        .resolve("catalog")
        .toString()
    );
    this.pathInventoryField.setText(
      directoryValues.dataDirectory()
        .resolve("inventory")
        .toString()
    );
    this.pathConfigField.setText(
      directoryValues.configurationDirectory()
        .resolve("preferences.xml")
        .toString()
    );
  }

  private void onPreferencesChanged(
    final CGXPreferences newPreferences)
  {
    this.debugEnabled.setSelected(newPreferences.debug().isDebugEnabled());
  }

  private void initializeSectionChooser()
  {
    this.preferenceSectionNames =
      FXCollections.observableArrayList();

    this.preferencesSections =
      Map.ofEntries(
        Map.entry(
          this.strings.format("ui.preferences.debugging"),
          this.debugPreferences
        ),
        Map.entry(
          this.strings.format("ui.preferences.paths"),
          this.pathPreferences
        )
      );

    this.preferenceSectionNames.setAll(
      this.preferencesSections.keySet()
        .stream()
        .sorted()
        .collect(Collectors.toList())
    );

    this.sectionChooser.setItems(this.preferenceSectionNames);
    this.sectionChooser.getSelectionModel()
      .selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.showSelectedSection(newValue);
      });
    this.sectionChooser.getSelectionModel().select(0);
  }

  private void showSelectedSection(
    final String sectionName)
  {
    if (sectionName != null) {
      this.preferencesSections.values()
        .forEach(section -> section.setVisible(false));
      this.preferencesSections.get(sectionName)
        .setVisible(true);
    }
  }

  @FXML
  private void onOKSelected()
  {
    this.subscriptions.dispose();
    this.sectionChooser.getScene().getWindow().hide();
  }

  @FXML
  private void onDebugEnabledChanged()
  {
    final var enabled = this.debugEnabled.isSelected();
    this.preferences.update(
      prefs -> prefs.withDebug(prefs.debug().withDebugEnabled(enabled))
    );
  }
}
