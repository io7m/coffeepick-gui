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
import com.io7m.coffeepick.gui.fx.CGXViewControllerFactoryType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import com.io7m.coffeepick.runtime.RuntimeConfiguration;
import com.io7m.coffeepick.runtime.RuntimeVersion;
import com.io7m.coffeepick.runtime.RuntimeVersionRange;
import com.io7m.coffeepick.runtime.RuntimeVersionRangeType;
import com.io7m.coffeepick.runtime.RuntimeVersions;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

public final class CGXViewControllerSearch
  implements CGXViewControllerType, Initializable
{
  private final CGXServiceDirectoryType services;
  private final CGXUIStringsType strings;
  private final SimpleObjectProperty<CGXRuntimeDescriptionFilterType> filter;
  private volatile CoffeePickSearch search;

  @FXML
  private CheckBox lowerBoundExclusive;
  @FXML
  private CheckBox upperBoundExclusive;
  @FXML
  private TextField platformField;
  @FXML
  private TextField archField;
  @FXML
  private TextField vmField;
  @FXML
  private TextField identifierField;
  @FXML
  private TextField repositoryField;
  @FXML
  private ComboBox<Optional<RuntimeConfiguration>> configurationField;
  @FXML
  private TextField versionLowerField;
  @FXML
  private TextField versionUpperField;
  @FXML
  private TextField versionRangeText;

  public CGXViewControllerSearch(
    final CGXViewControllerFactoryType inControllers,
    final CGXServiceDirectoryType inServices)
  {
    this.services =
      Objects.requireNonNull(inServices, "inServices");
    this.strings =
      CGXUIStrings.of(CGXUIStrings.getResourceBundle());

    this.search =
      CoffeePickSearch.builder()
        .build();
    this.filter =
      new SimpleObjectProperty<>(
        CGXRuntimeDescriptionSearchFilter.of(this.search)
      );
  }

  private static Optional<String> ofNonBlank(
    final String text)
  {
    if (text.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(text);
  }

  public ObservableValue<CGXRuntimeDescriptionFilterType> filter()
  {
    return this.filter;
  }

  private void updateSearch(
    final Function<CoffeePickSearch, CoffeePickSearch> updater)
  {
    this.search = updater.apply(this.search);
    this.filter.set(CGXRuntimeDescriptionSearchFilter.of(this.search));
  }

  @FXML
  private void onPlatformFieldChanged()
  {
    this.updateSearch(currentSearch -> {
      return CoffeePickSearch.builder()
        .from(currentSearch)
        .setPlatform(ofNonBlank(this.platformField.getText().trim()))
        .build();
    });
  }

  @FXML
  private void onArchitectureFieldChanged()
  {
    this.updateSearch(currentSearch -> {
      return CoffeePickSearch.builder()
        .from(currentSearch)
        .setArchitecture(ofNonBlank(this.archField.getText().trim()))
        .build();
    });
  }

  @FXML
  private void onVMFieldChanged()
  {
    this.updateSearch(currentSearch -> {
      return CoffeePickSearch.builder()
        .from(currentSearch)
        .setVm(ofNonBlank(this.vmField.getText().trim()))
        .build();
    });
  }

  @FXML
  private void onConfigurationFieldChanged()
  {
    this.updateSearch(currentSearch -> {
      return CoffeePickSearch.builder()
        .from(currentSearch)
        .setConfiguration(this.configurationField.getValue())
        .build();
    });
  }

  @FXML
  private void onVersionFieldChanged()
  {
    RuntimeVersion versionLower;
    RuntimeVersion versionUpper;
    Optional<RuntimeVersionRange> range;

    final var lowerText =
      this.versionLowerField.getText();
    final var upperText =
      this.versionUpperField.getText();

    try {
      versionLower = RuntimeVersions.parse(lowerText);
    } catch (final Exception e) {
      versionLower =
        RuntimeVersion.builder()
          .setMajor(BigInteger.valueOf(-1L))
          .setMinor(BigInteger.ZERO)
          .setPatch(BigInteger.ZERO)
          .build();
    }

    try {
      versionUpper = RuntimeVersions.parse(upperText);
    } catch (final Exception e) {
      versionUpper =
        RuntimeVersion.builder()
          .setMajor(BigInteger.valueOf(Long.MAX_VALUE))
          .setMinor(BigInteger.valueOf(Long.MAX_VALUE))
          .setPatch(BigInteger.valueOf(Long.MAX_VALUE))
          .build();
    }

    try {
      final var rangeBuilder =
        RuntimeVersionRange.builder()
          .setLowerExclusive(this.lowerBoundExclusive.isSelected())
          .setUpperExclusive(this.upperBoundExclusive.isSelected())
          .setLower(versionLower)
          .setUpper(versionUpper);
      range = Optional.of(rangeBuilder.build());
    } catch (final Exception e) {
      range = Optional.empty();
    }

    if (!lowerText.isBlank() && !upperText.isBlank()) {
      this.versionRangeText.setText(
        range.map(RuntimeVersionRangeType::toExternalString)
          .orElse("")
      );
    } else {
      this.versionRangeText.setText("");
    }

    final Optional<RuntimeVersionRange> finalRange = range;
    this.updateSearch(currentSearch -> {
      return CoffeePickSearch.builder()
        .from(currentSearch)
        .setVersionRange(finalRange)
        .build();
    });
  }

  @FXML
  private void onIdentifierFieldChanged()
  {
    this.updateSearch(currentSearch -> {
      return CoffeePickSearch.builder()
        .from(currentSearch)
        .setId(this.identifierField.getText().trim())
        .build();
    });
  }

  @FXML
  private void onRepositoryFieldChanged()
  {
    this.updateSearch(currentSearch -> {
      return CoffeePickSearch.builder()
        .from(currentSearch)
        .setRepository(ofNonBlank(this.repositoryField.getText().trim()))
        .build();
    });
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    this.versionLowerField.textProperty()
      .addListener((observable, oldValue, newValue) -> this.onVersionFieldChanged());
    this.versionUpperField.textProperty()
      .addListener((observable, oldValue, newValue) -> this.onVersionFieldChanged());

    this.lowerBoundExclusive.setOnAction(event -> this.onVersionFieldChanged());
    this.upperBoundExclusive.setOnAction(event -> this.onVersionFieldChanged());

    final List<Optional<RuntimeConfiguration>> configItems =
      List.of(
        Optional.empty(),
        Optional.of(RuntimeConfiguration.JDK),
        Optional.of(RuntimeConfiguration.JRE)
      );

    this.configurationField.setConverter(
      new CGXJVMConfigurationConverter(this.strings));
    this.configurationField.setItems(
      FXCollections.observableArrayList(configItems));
    this.configurationField.getSelectionModel().select(0);
    this.configurationField.setOnAction(event -> {
      this.onConfigurationFieldChanged();
    });

    this.identifierField.textProperty()
      .addListener((observable, oldValue, newValue) -> this.onIdentifierFieldChanged());
    this.archField.textProperty()
      .addListener((observable, oldValue, newValue) -> this.onArchitectureFieldChanged());
    this.platformField.textProperty()
      .addListener((observable, oldValue, newValue) -> this.onPlatformFieldChanged());
    this.repositoryField.textProperty()
      .addListener((observable, oldValue, newValue) -> this.onRepositoryFieldChanged());
    this.vmField.textProperty()
      .addListener((observable, oldValue, newValue) -> this.onVMFieldChanged());
  }
}
