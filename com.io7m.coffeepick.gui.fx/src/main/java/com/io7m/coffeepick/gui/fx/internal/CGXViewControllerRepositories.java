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

import com.io7m.coffeepick.gui.controller.CGXControllerType;
import com.io7m.coffeepick.gui.fx.CGXViewControllerFactoryType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import com.io7m.coffeepick.repository.spi.RuntimeRepositoryType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * The view controller for the repository window.
 */

public final class CGXViewControllerRepositories
  implements CGXViewControllerType
{
  private final CGXControllerType controller;
  private final CGXServiceDirectoryType services;
  private final CGXUIStringsType strings;
  private final CompositeDisposable subscriptions;
  private final ObservableList<RuntimeRepositoryType> repositories;

  @FXML private Button updateButton;
  @FXML private ComboBox<RuntimeRepositoryType> repositoryComboBox;
  @FXML private Hyperlink brandingSiteField;
  @FXML private ImageView brandingImage;
  @FXML private Label brandingSubtitleField;
  @FXML private Label brandingTitleField;
  @FXML private TextField idField;
  @FXML private TextField runtimeCountField;
  @FXML private TextField updatedField;

  public CGXViewControllerRepositories(
    final CGXViewControllerFactoryType factory,
    final CGXServiceDirectoryType inServices)
  {
    this.services =
      Objects.requireNonNull(inServices, "inServices");
    this.controller =
      inServices.requireService(CGXControllerType.class);
    this.strings =
      CGXUIStrings.of(CGXUIStrings.getResourceBundle());
    this.subscriptions =
      new CompositeDisposable();
    this.repositories =
      FXCollections.observableArrayList();
  }

  @FXML
  private void onUpdateSelected()
  {
    final var repository =
      this.repositoryComboBox.getSelectionModel()
        .getSelectedItem();

    this.controller.repositoryUpdate(repository.description().id())
      .future()
      .thenRun(() -> {
        Platform.runLater(() -> {
          this.onConfigureURIForRepository(
            this.repositoryComboBox.getSelectionModel().getSelectedItem()
          );
        });
      });
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    this.subscriptions.add(
      this.controller.taskRunning()
        .observable()
        .subscribe(event -> Platform.runLater(this::onAdjustUpdateButton))
    );

    this.brandingSiteField.setText("");
    this.brandingSubtitleField.setText("");
    this.brandingTitleField.setText("");
    this.brandingImage.setImage(null);

    this.repositoryComboBox.setItems(this.repositories);
    this.repositoryComboBox.setConverter(
      new RuntimeRepositoryTypeStringConverter());
    this.repositoryComboBox.setCellFactory(
      new CGXRuntimeRepositoryCellFactory());
    this.repositoryComboBox.getSelectionModel()
      .selectedItemProperty()
      .addListener(
        (observable, oldValue, newValue) ->
          this.onConfigureURIForRepository(newValue)
      );

    this.subscriptions.add(
      this.controller.repositories()
        .observable()
        .subscribe(newRepositories -> {
          this.repositories.setAll(newRepositories);
          this.repositoryComboBox.getSelectionModel().select(0);
        })
    );
  }

  private void onAdjustUpdateButton()
  {
    CGXFXThread.checkIsUIThread();

    final boolean isTaskRunning =
      this.controller.taskRunning().value().booleanValue();
    final boolean isNothingSelected =
      this.repositoryComboBox.getSelectionModel().isEmpty();
    this.updateButton.setDisable(isTaskRunning || isNothingSelected);
  }

  private void onConfigureURIForRepository(
    final RuntimeRepositoryType repository)
  {
    CGXFXThread.checkIsUIThread();

    this.onAdjustUpdateButton();

    if (repository != null) {
      final var description = repository.description();
      this.idField.setText(description.id().toString());
      this.updatedField.setText(
        description.updated()
          .map(DateTimeFormatter.ISO_OFFSET_DATE_TIME::format)
          .orElse("")
      );
      this.runtimeCountField.setText(
        String.valueOf(repository.runtimes().size())
      );

      final var branding = description.branding();
      this.brandingSiteField.setText(branding.site().toString());
      this.brandingSubtitleField.setText(branding.subtitle());
      this.brandingTitleField.setText(branding.title());
      this.brandingImage.setImage(new Image(
        branding.logo().toString()
      ));
    }
  }

  private static final class RuntimeRepositoryTypeStringConverter
    extends StringConverter<RuntimeRepositoryType>
  {
    RuntimeRepositoryTypeStringConverter()
    {

    }

    @Override
    public String toString(
      final RuntimeRepositoryType object)
    {
      if (object == null) {
        return "";
      }
      return String.format(
        "%s - %s",
        object.provider().name(),
        object.description().branding().title());
    }

    @Override
    public RuntimeRepositoryType fromString(
      final String string)
    {
      return null;
    }
  }
}
