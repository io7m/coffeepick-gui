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
import com.io7m.coffeepick.runtime.RuntimeDescription;
import com.io7m.coffeepick.runtime.RuntimeDescriptionType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.apache.commons.io.FileUtils;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class CGXViewControllerRuntime
  implements CGXViewControllerType, Initializable
{
  private final CGXControllerType controller;
  private final CGXServiceDirectoryType services;
  private final CGXUIStringsType strings;
  private final CompositeDisposable subscriptions;
  private final ObservableList<RuntimeDescription> runtimes;
  private final SimpleIntegerProperty runtimeIndex;

  @FXML private Button buttonDownloadAll;
  @FXML private Button buttonNext;
  @FXML private Button buttonPrevious;
  @FXML private Button buttonWeb;
  @FXML private ImageView icon;
  @FXML private Label title;
  @FXML private TextField architectureField;
  @FXML private TextField archiveHashAlgorithmField;
  @FXML private TextField archiveHashValueField;
  @FXML private TextField archiveSizeField;
  @FXML private TextField archiveURIField;
  @FXML private TextField buildDateField;
  @FXML private TextField configurationField;
  @FXML private TextField identifierField;
  @FXML private TextField platformField;
  @FXML private TextField repositoryField;
  @FXML private TextField tagsField;
  @FXML private TextField versionField;
  @FXML private TextField vmField;
  @FXML private Pane catalogButtons;
  @FXML private Pane inventoryButtons;

  private List<TextField> fields;
  private Image imageJDK;
  private Image imageJRE;

  public CGXViewControllerRuntime(
    final CGXViewControllerFactoryType inControllers,
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
    this.runtimes =
      FXCollections.observableArrayList();
    this.runtimeIndex =
      new SimpleIntegerProperty();
  }

  public void setItems(
    final List<RuntimeDescription> newRuntimes)
  {
    this.runtimes.setAll(
      Objects.requireNonNull(newRuntimes, "newRuntimes")
    );
    this.runtimeIndex.set(0);
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    this.buttonWeb.setDisable(true);

    this.imageJDK =
      new Image(CGXViewControllerRuntime.class.getResource(
        "/com/io7m/coffeepick/gui/fx/icons/jdk64.png"
      ).toExternalForm());
    this.imageJRE =
      new Image(CGXViewControllerRuntime.class.getResource(
        "/com/io7m/coffeepick/gui/fx/icons/jre64.png"
      ).toExternalForm());

    this.fields = List.of(
      this.architectureField,
      this.archiveHashAlgorithmField,
      this.archiveHashValueField,
      this.archiveSizeField,
      this.archiveURIField,
      this.buildDateField,
      this.configurationField,
      this.identifierField,
      this.platformField,
      this.repositoryField,
      this.tagsField,
      this.versionField,
      this.vmField
    );

    this.title.setText("");
    this.buttonPrevious.setDisable(true);
    this.buttonNext.setDisable(true);

    this.runtimeIndex.addListener(
      (observable, oldValue, newValue) ->
        this.configureForRuntime(newValue.intValue()));
    this.runtimes.addListener(
      (ListChangeListener<RuntimeDescription>) o ->
        this.configureForRuntime(this.runtimeIndex.getValue().intValue()));

    this.subscriptions.add(
      this.controller.taskRunning()
        .observable()
        .subscribe(event -> Platform.runLater(this::onActionButtonsReconfigure))
    );
  }

  private void onActionButtonsReconfigure()
  {
    CGXFXThread.checkIsUIThread();

    final boolean isRunning =
      this.controller.taskRunning().value().booleanValue();

    this.buttonDownloadAll.setDisable(isRunning);
  }

  private String titleOf(
    final int index,
    final int size)
  {
    return this.strings.format(
      "ui.details.title",
      Integer.valueOf(index),
      Integer.valueOf(size)
    );
  }

  private void configureForRuntime(final int index)
  {
    this.buttonNext.setDisable(this.runtimes.size() <= 1);
    this.buttonPrevious.setDisable(this.runtimes.size() <= 1);

    if (index >= this.runtimes.size() || index < 0) {
      this.title.setText(this.titleOf(1, 1));
      this.fields.forEach(TextInputControl::clear);
      return;
    }

    this.title.setText(this.titleOf(index + 1, this.runtimes.size()));

    final var runtime = this.runtimes.get(index);
    this.architectureField.setText(runtime.architecture());
    this.archiveHashAlgorithmField.setText(runtime.archiveHash().algorithm());
    this.archiveHashValueField.setText(runtime.archiveHash().value());
    this.archiveSizeField.setText(FileUtils.byteCountToDisplaySize(runtime.archiveSize()));
    this.archiveURIField.setText(runtime.archiveURI().toString());
    this.configurationField.setText(runtime.configuration().configurationName());
    this.identifierField.setText(runtime.id());
    this.platformField.setText(runtime.platform());
    this.repositoryField.setText(runtime.repository().toString());
    this.versionField.setText(runtime.version().toExternalString());
    this.tagsField.setText(String.join(", ", runtime.tags()));
    this.vmField.setText(runtime.vm());

    runtime.build().ifPresent(build -> {
      this.buildDateField.setText(
        build.time().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      );
    });

    switch (runtime.configuration()) {
      case JRE:
        this.icon.setImage(this.imageJRE);
        break;
      case JDK:
        this.icon.setImage(this.imageJDK);
        break;
    }
  }

  @FXML
  private void onButtonDownloadAllSelected()
  {
    this.controller.catalogDownload(
      this.runtimes.stream()
        .map(RuntimeDescriptionType::id)
        .collect(Collectors.toSet())
    );

    this.title.getScene().getWindow().hide();
  }

  @FXML
  private void onButtonUnpackSelected()
  {

  }

  @FXML
  private void onButtonDeleteSelected()
  {

  }

  @FXML
  private void onWebButtonPressed()
  {
    final var index = this.runtimeIndex.get();
    if (index >= this.runtimes.size()) {
      return;
    }
  }

  @FXML
  private void onNextButtonPressed()
  {
    this.runtimeIndex.set((this.runtimeIndex.get() + 1) % this.runtimes.size());
  }

  @FXML
  private void onPreviousButtonPressed()
  {
    if (this.runtimeIndex.get() == 0) {
      this.runtimeIndex.set(this.runtimes.size() - 1);
    } else {
      this.runtimeIndex.set(this.runtimeIndex.get() - 1);
    }
  }

  public void setContext(
    final CGXViewControllerRuntimeContext context)
  {
    Objects.requireNonNull(context, "context");

    switch (context) {
      case CONTEXT_INVENTORY: {
        this.catalogButtons.setVisible(false);
        this.inventoryButtons.setVisible(true);
        break;
      }
      case CONTEXT_CATALOG: {
        this.catalogButtons.setVisible(true);
        this.inventoryButtons.setVisible(false);
        break;
      }
    }
  }
}
