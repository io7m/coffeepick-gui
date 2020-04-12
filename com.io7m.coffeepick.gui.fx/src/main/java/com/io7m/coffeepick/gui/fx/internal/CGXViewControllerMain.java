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

import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskCancelled;
import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskCompleted;
import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskFailed;
import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskRunning;
import com.io7m.coffeepick.gui.controller.CGXControllerEventTaskStarted;
import com.io7m.coffeepick.gui.controller.CGXControllerType;
import com.io7m.coffeepick.gui.filechooser.api.CGXFileChoosersType;
import com.io7m.coffeepick.gui.fx.CGXViewControllerFactoryType;
import com.io7m.coffeepick.gui.preferences.CGXPreferences;
import com.io7m.coffeepick.gui.preferences.CGXPreferencesControllerType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import com.io7m.coffeepick.runtime.RuntimeDescription;
import com.io7m.coffeepick.runtime.RuntimeDescriptionType;
import com.io7m.jwheatsheaf.api.JWFileChooserAction;
import com.io7m.jwheatsheaf.api.JWFileChooserConfiguration;
import com.io7m.jwheatsheaf.api.JWFileChoosersType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.io7m.coffeepick.gui.fx.internal.CGXViewControllerRuntimeContext.CONTEXT_CATALOG;
import static com.io7m.coffeepick.gui.fx.internal.CGXViewControllerRuntimeContext.CONTEXT_INVENTORY;

/**
 * The view controller for the main window.
 */

public final class CGXViewControllerMain
  implements CGXViewControllerType
{
  private final CGXControllerType controller;
  private final CGXPreferencesControllerType preferences;
  private final CGXServiceDirectoryType services;
  private final CGXUIStringsType strings;
  private final CGXViewControllerFactoryType viewControllers;
  private final CompositeDisposable subscriptions;
  private final JWFileChoosersType fileChoosers;

  @FXML private Button catalogDetailsButton;
  @FXML private Button inventoryDeleteButton;
  @FXML private Button inventoryDetailsButton;
  @FXML private Button inventoryUnpackButton;
  @FXML private CGXViewControllerSearch catalogSearchController;
  @FXML private CGXViewControllerSearch inventorySearchController;
  @FXML private Label catalogMatchLabel;
  @FXML private Label inventoryMatchLabel;
  @FXML private Label progressLabel;
  @FXML private Pane catalogSearch;
  @FXML private Pane inventorySearch;
  @FXML private Pane mainPane;
  @FXML private ProgressIndicator progressIndicator;
  @FXML private Tab debugTab;
  @FXML private TabPane mainTabPane;
  @FXML private TableView<RuntimeDescription> catalogTable;
  @FXML private TableView<RuntimeDescription> inventoryTable;

  private CGXRuntimeDescriptionList catalogList;
  private CGXRuntimeDescriptionList inventoryList;

  public CGXViewControllerMain(
    final CGXViewControllerFactoryType inControllers,
    final CGXServiceDirectoryType inServices)
  {
    this.viewControllers =
      Objects.requireNonNull(inControllers, "inControllers");
    this.services =
      Objects.requireNonNull(inServices, "inServices");
    this.controller =
      inServices.requireService(CGXControllerType.class);
    this.fileChoosers =
      inServices.requireService(CGXFileChoosersType.class).fileChoosers();
    this.preferences =
      inServices.requireService(CGXPreferencesControllerType.class);

    this.strings =
      CGXUIStrings.of(CGXUIStrings.getResourceBundle());
    this.subscriptions =
      new CompositeDisposable();
  }

  /**
   * Create an FXML loader for the main view controller.
   *
   * @param viewControllers A view controller factory
   *
   * @return An FXML loader
   */

  public static FXMLLoader createLoader(
    final CGXViewControllerFactoryType viewControllers)
  {
    final var mainXML =
      CGXViewControllerMain.class.getResource("coffeepick.fxml");

    return new FXMLLoader(
      mainXML,
      CGXUIStrings.getResourceBundle(),
      null,
      viewControllers::createController
    );
  }

  @FXML
  private void onCatalogDetailsSelected()
    throws IOException
  {
    final var selected =
      this.catalogTable.getSelectionModel()
        .getSelectedItems();

    final var mainXML =
      CGXViewControllerMain.class.getResource("runtime.fxml");

    final var loader =
      new FXMLLoader(
        mainXML,
        CGXUIStrings.getResourceBundle(),
        null,
        this.viewControllers::createController
      );

    final Stage stage = new Stage();
    final Pane pane = loader.load();
    pane.getStylesheets().add(CGXCSS.stylesheet());

    final CGXViewControllerRuntime runtimeController = loader.getController();
    runtimeController.setItems(selected);
    runtimeController.setContext(CONTEXT_CATALOG);

    stage.setMinWidth(320.0);
    stage.setMinHeight(240.0);
    stage.setScene(new Scene(pane));
    stage.titleProperty().setValue("Runtime Details");
    stage.show();
  }

  @FXML
  private void onInventoryDetailsSelected()
    throws IOException
  {
    final var selected =
      this.inventoryTable.getSelectionModel()
        .getSelectedItems();

    final var mainXML =
      CGXViewControllerMain.class.getResource("runtime.fxml");

    final var loader =
      new FXMLLoader(
        mainXML,
        CGXUIStrings.getResourceBundle(),
        null,
        this.viewControllers::createController
      );

    final Stage stage = new Stage();
    final Pane pane = loader.load();
    pane.getStylesheets().add(CGXCSS.stylesheet());

    final CGXViewControllerRuntime runtimeController = loader.getController();
    runtimeController.setItems(selected);
    runtimeController.setContext(CONTEXT_INVENTORY);

    stage.setMinWidth(320.0);
    stage.setMinHeight(240.0);
    stage.setScene(new Scene(pane));
    stage.titleProperty().setValue("Runtime Details");
    stage.show();
  }

  @FXML
  private void onInventoryDeleteSelected()
    throws IOException
  {
    final var delete =
      new ButtonType(
        this.strings.format("ui.inventory.delete.confirm"),
        ButtonBar.ButtonData.OK_DONE
      );

    final var alert =
      new Alert(
        Alert.AlertType.CONFIRMATION,
        "",
        ButtonType.CANCEL,
        delete
      );

    final var selectedItems =
      List.copyOf(this.inventoryTable.getSelectionModel().getSelectedItems());
    final var selectedCount =
      selectedItems.size();

    alert.setContentText(
      this.strings.format(
        "ui.inventory.delete.confirmMessage",
        Integer.valueOf(selectedCount)
      )
    );
    alert.setTitle(
      this.strings.format("ui.inventory.delete.confirmTitle")
    );

    final var dialogPane = alert.getDialogPane();
    dialogPane.getStylesheets().add(CGXCSS.stylesheet());
    dialogPane.setHeaderText(null);

    final var result = alert.showAndWait();
    if (Objects.equals(result, Optional.of(delete))) {
      this.controller.inventoryDelete(
        selectedItems.stream()
          .map(RuntimeDescriptionType::id)
          .collect(Collectors.toSet())
      );
    }
  }

  @FXML
  private void onInventoryUnpackSelected()
    throws IOException
  {
    final var selectedItems =
      List.copyOf(this.inventoryTable.getSelectionModel().getSelectedItems());

    final var configuration =
      JWFileChooserConfiguration.builder()
        .setFileSystem(FileSystems.getDefault())
        .setAction(JWFileChooserAction.OPEN_EXISTING_SINGLE)
        .setCssStylesheet(new URL(CGXCSS.stylesheet()))
        .build();

    final var fileChooser =
      this.fileChoosers.create(
        this.mainPane.getScene().getWindow(),
        configuration
      );

    final var paths = fileChooser.showAndWait();
    if (!paths.isEmpty()) {
      final var path = paths.get(0);
      this.controller.inventoryUnpack(
        selectedItems.stream()
          .map(RuntimeDescriptionType::id)
          .collect(Collectors.toSet()),
        path
      );
    }
  }

  @FXML
  private void onMenuQuitSelected()
  {
    Platform.exit();
  }

  @FXML
  private void onMenuPreferencesSelected()
    throws IOException
  {
    final var mainXML =
      CGXViewControllerMain.class.getResource("preferences.fxml");

    final var loader =
      new FXMLLoader(
        mainXML,
        CGXUIStrings.getResourceBundle(),
        null,
        this.viewControllers::createController
      );

    final Stage stage = new Stage();
    final Pane pane = loader.load();
    pane.getStylesheets().add(CGXCSS.stylesheet());

    stage.setMinWidth(320.0);
    stage.setMinHeight(240.0);
    stage.setScene(new Scene(pane));
    stage.titleProperty().setValue("Preferences…");
    stage.show();
  }

  @FXML
  private void onMenuHelpAboutSelected()
    throws IOException
  {
    final var aboutXML =
      CGXViewControllerMain.class.getResource("about.fxml");

    final var loader =
      new FXMLLoader(
        aboutXML,
        CGXUIStrings.getResourceBundle(),
        null,
        this.viewControllers::createController
      );

    final Stage stage = new Stage();
    final Pane pane = loader.load();
    pane.getStylesheets().add(CGXCSS.stylesheet());

    stage.titleProperty().setValue(this.strings.format("ui.menu.help.about"));
    stage.setMinWidth(320.0);
    stage.setMinHeight(240.0);
    stage.setScene(new Scene(pane));
    stage.show();
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    this.catalogList =
      new CGXRuntimeDescriptionList(
        this.catalogSearchController.filter().getValue()
      );
    this.inventoryList =
      new CGXRuntimeDescriptionList(
        this.inventorySearchController.filter().getValue()
      );

    this.initializeCatalogTable();
    this.initializeInventoryTable();

    this.catalogSearchController.filter()
      .addListener((observable, oldValue, newValue) ->
                     this.catalogList.setFilter(newValue)
      );
    this.inventorySearchController.filter()
      .addListener((observable, oldValue, newValue) ->
                     this.inventoryList.setFilter(newValue)
      );

    this.catalogList.items()
      .addListener((ListChangeListener<RuntimeDescription>) observable -> {
        this.onCatalogListChanged();
      });
    this.inventoryList.items()
      .addListener((ListChangeListener<RuntimeDescription>) observable -> {
        this.onInventoryListChanged();
      });

    this.progressLabel.setText("");
    this.progressIndicator.setVisible(false);

    this.subscriptions.add(
      this.controller.events()
        .ofType(CGXControllerEventTaskStarted.class)
        .subscribe(this::onTaskStartedEvent)
    );

    this.subscriptions.add(
      this.controller.events()
        .ofType(CGXControllerEventTaskFailed.class)
        .subscribe(this::onTaskFailedEvent)
    );

    this.subscriptions.add(
      this.controller.events()
        .ofType(CGXControllerEventTaskRunning.class)
        .subscribe(this::onTaskRunningEvent)
    );

    this.subscriptions.add(
      this.controller.events()
        .ofType(CGXControllerEventTaskCompleted.class)
        .subscribe(this::onTaskCompletedEvent)
    );

    this.subscriptions.add(
      this.controller.events()
        .ofType(CGXControllerEventTaskCancelled.class)
        .subscribe(this::onTaskCancelledEvent)
    );

    this.catalogMatchLabel.setText("");
    this.inventoryMatchLabel.setText("");

    this.subscriptions.add(
      this.controller.inventory()
        .observable()
        .subscribe(data -> this.onInventoryMapChanged())
    );
    this.subscriptions.add(
      this.controller.catalog()
        .observable()
        .subscribe(data -> this.onCatalogMapChanged())
    );

    this.subscriptions.add(
      this.preferences.preferences()
        .observable()
        .subscribe(this::onPreferencesUpdated)
    );
  }

  private void onPreferencesUpdated(
    final CGXPreferences newPreferences)
  {
    Platform.runLater(() -> {
      final var tabs = this.mainTabPane.getTabs();
      if (newPreferences.debug().isDebugEnabled()) {
        if (!tabs.contains(this.debugTab)) {
          tabs.add(this.debugTab);
        }
      } else {
        tabs.remove(this.debugTab);
      }
    });
  }

  private void onCatalogMapChanged()
  {
    Platform.runLater(() -> {
      this.catalogList.setItems(
        this.controller.catalog()
          .value()
          .values()
          .stream()
          .sorted(Comparator.comparing(RuntimeDescriptionType::id))
          .collect(Collectors.toList())
      );
    });
  }

  private void onInventoryMapChanged()
  {
    Platform.runLater(() -> {
      this.inventoryList.setItems(
        this.controller.inventory()
          .value()
          .values()
          .stream()
          .sorted(Comparator.comparing(RuntimeDescriptionType::id))
          .collect(Collectors.toList())
      );
    });
  }

  private void onCatalogListChanged()
  {
    this.catalogMatchLabel.setText(
      this.strings.format(
        "ui.runtimesCount",
        Integer.valueOf(this.catalogList.items().size()))
    );
  }

  private void onInventoryListChanged()
  {
    this.inventoryMatchLabel.setText(
      this.strings.format(
        "ui.runtimesCount",
        Integer.valueOf(this.inventoryList.items().size()))
    );
  }

  private void initializeInventoryTable()
  {
    this.inventoryTable.setItems(this.inventoryList.items());

    final var tableColumns =
      this.inventoryTable.getColumns();

    final var tableConfigurationColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(0);
    final var tableIdColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(1);
    final var tablePlatformColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(2);
    final var tableArchColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(3);
    final var tableVersionColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(4);
    final var tableVMColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(5);
    final var tableProviderColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(6);

    tableIdColumn.setCellFactory(
      param -> new CGXTableIdCell());
    tablePlatformColumn.setCellFactory(
      param -> new CGXTablePlatformCell());
    tableArchColumn.setCellFactory(
      param -> new CGXTableArchitectureCell());
    tableVersionColumn.setCellFactory(
      param -> new CGXTableVersionCell());
    tableVMColumn.setCellFactory(
      param -> new CGXTableVMCell());
    tableConfigurationColumn.setCellFactory(
      new CGXTableConfigurationCellFactory(this.strings));
    tableProviderColumn.setCellFactory(
      param -> new CGXTableProviderCell());

    tableIdColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tablePlatformColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableArchColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableVersionColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableVMColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableConfigurationColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableProviderColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));

    final var tableSelectionModel =
      this.inventoryTable.getSelectionModel();

    tableSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
    tableSelectionModel.selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        final var noneSelected =
          tableSelectionModel.getSelectedItems().isEmpty();
        this.inventoryDetailsButton.setDisable(noneSelected);
        this.inventoryDeleteButton.setDisable(noneSelected);
        this.inventoryUnpackButton.setDisable(noneSelected);
      });
  }

  private void initializeCatalogTable()
  {
    this.catalogTable.setItems(this.catalogList.items());

    final var tableColumns =
      this.catalogTable.getColumns();

    final var tableConfigurationColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(0);
    final var tableIdColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(1);
    final var tablePlatformColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(2);
    final var tableArchColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(3);
    final var tableVersionColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(4);
    final var tableVMColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(5);
    final var tableProviderColumn =
      (TableColumn<RuntimeDescription, RuntimeDescription>) tableColumns.get(6);

    tableIdColumn.setCellFactory(
      param -> new CGXTableIdCell());
    tablePlatformColumn.setCellFactory(
      param -> new CGXTablePlatformCell());
    tableArchColumn.setCellFactory(
      param -> new CGXTableArchitectureCell());
    tableVersionColumn.setCellFactory(
      param -> new CGXTableVersionCell());
    tableVMColumn.setCellFactory(
      param -> new CGXTableVMCell());
    tableConfigurationColumn.setCellFactory(
      new CGXTableConfigurationCellFactory(this.strings));
    tableProviderColumn.setCellFactory(
      param -> new CGXTableProviderCell());

    tableIdColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tablePlatformColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableArchColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableVersionColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableVMColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableConfigurationColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));
    tableProviderColumn.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue()));

    final var tableSelectionModel =
      this.catalogTable.getSelectionModel();

    tableSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
    tableSelectionModel.selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        this.catalogDetailsButton.setDisable(
          tableSelectionModel.getSelectedItems().isEmpty()
        );
      });
  }

  private void onTaskCompletedEvent(
    final CGXControllerEventTaskCompleted event)
  {
    Platform.runLater(() -> {
      this.progressLabel.setText("");
      this.progressIndicator.setVisible(false);
    });
  }

  private void onTaskCancelledEvent(
    final CGXControllerEventTaskCancelled event)
  {
    Platform.runLater(() -> {
      this.progressLabel.setText("");
      this.progressIndicator.setVisible(false);
    });
  }

  private void onTaskRunningEvent(
    final CGXControllerEventTaskRunning event)
  {
    Platform.runLater(() -> {
      this.progressLabel.setText(event.description());
      this.progressIndicator.setVisible(true);
    });
  }

  private void onTaskFailedEvent(
    final CGXControllerEventTaskFailed event)
  {
    Platform.runLater(() -> {
      this.progressLabel.setText(event.description());
      this.progressIndicator.setVisible(false);
    });
  }

  private void onTaskStartedEvent(
    final CGXControllerEventTaskStarted event)
  {
    Platform.runLater(() -> {
      this.progressLabel.setText(event.description());
      this.progressIndicator.setVisible(true);
    });
  }
}
