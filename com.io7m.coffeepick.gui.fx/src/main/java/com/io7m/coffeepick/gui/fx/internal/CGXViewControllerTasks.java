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
import com.io7m.coffeepick.gui.controller.CGXControllerTaskType;
import com.io7m.coffeepick.gui.controller.CGXControllerType;
import com.io7m.coffeepick.gui.fx.CGXViewControllerFactoryType;
import com.io7m.coffeepick.gui.services.api.CGXServiceDirectoryType;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public final class CGXViewControllerTasks
  implements CGXViewControllerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CGXViewControllerTasks.class);

  private final CGXServiceDirectoryType services;
  private final CGXControllerType controller;
  private final CompositeDisposable subscriptions;
  private final ObservableList<CGXControllerTaskType> taskListItems;

  @FXML private Button cancelButton;
  @FXML private Label taskDescription;
  @FXML private ProgressBar progressMajor;
  @FXML private ProgressBar progressMinor;
  @FXML private TextArea taskDetails;
  @FXML private ListView<CGXControllerTaskType> taskList;

  public CGXViewControllerTasks(
    final CGXViewControllerFactoryType inControllers,
    final CGXServiceDirectoryType inServices)
  {
    this.services =
      Objects.requireNonNull(inServices, "inServices");
    this.controller =
      inServices.requireService(CGXControllerType.class);
    this.subscriptions =
      new CompositeDisposable();

    this.taskListItems =
      FXCollections.observableArrayList();
  }

  private static void animateProperty(
    final DoubleProperty prop,
    final double value)
  {
    final var timeline = new Timeline();
    final var keyValue = new KeyValue(prop, Double.valueOf(value));
    final var keyFrame = new KeyFrame(new Duration(50.0), keyValue);
    timeline.getKeyFrames().add(keyFrame);
    timeline.play();
  }

  @Override
  public void initialize(
    final URL location,
    final ResourceBundle resources)
  {
    Objects.requireNonNull(this.taskDescription, "taskDescription");
    Objects.requireNonNull(this.progressMajor, "progressMajor");
    Objects.requireNonNull(this.progressMinor, "progressMinor");
    Objects.requireNonNull(this.taskDetails, "taskDetails");

    this.taskList.setItems(this.taskListItems);
    this.taskList.setCellFactory(param -> new CGXControllerTaskListCell());
    this.taskList.getSelectionModel()
      .selectedItemProperty()
      .addListener(
        (observable, oldValue, newValue) -> this.onTaskSelected(newValue)
      );

    this.taskDescription.setText("");
    this.progressMinor.setProgress(0.0);
    this.progressMajor.setProgress(0.0);

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

    this.subscriptions.add(
      this.controller.tasks()
        .observable()
        .subscribe(repositories -> {
          Platform.runLater(() -> {
            this.taskListItems.setAll(repositories);
          });
        })
    );
  }

  private void onTaskSelected(
    final CGXControllerTaskType task)
  {
    this.taskDetails.clear();

    if (task == null) {
      return;
    }

    final Throwable taskException = task.exception();
    if (taskException != null) {
      final var stringWriter = new StringWriter();
      final var writer = new PrintWriter(stringWriter);
      taskException.printStackTrace(writer);
      this.taskDetails.setText(stringWriter.toString());
    }
  }

  @FXML
  private void onCancelSelected()
  {
    LOG.debug("cancel");

    this.controller.cancel();
  }

  private void onTaskCancelledEvent(
    final CGXControllerEventTaskCancelled event)
  {
    Platform.runLater(() -> {
      this.cancelButton.setDisable(true);

      this.taskDescription.setText(event.description());
      this.progressMinor.setProgress(0.0);
      this.progressMajor.setProgress(0.0);
    });
  }

  private void onTaskCompletedEvent(
    final CGXControllerEventTaskCompleted event)
  {
    Platform.runLater(() -> {
      this.cancelButton.setDisable(true);

      this.taskDescription.setText(event.description());
      this.progressMinor.setProgress(0.0);
      this.progressMajor.setProgress(0.0);
    });
  }

  private void onTaskRunningEvent(
    final CGXControllerEventTaskRunning event)
  {
    Platform.runLater(() -> {
      this.cancelButton.setDisable(false);

      this.taskDescription.setText(event.description());
      event.progressMajor()
        .ifPresentOrElse(
          value -> animateProperty(
            this.progressMajor.progressProperty(),
            value),
          () -> this.progressMajor.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS)
        );
      event.progressMinor()
        .ifPresentOrElse(
          value -> animateProperty(
            this.progressMinor.progressProperty(),
            value),
          () -> this.progressMinor.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS)
        );
    });
  }

  private void onTaskFailedEvent(
    final CGXControllerEventTaskFailed event)
  {
    Platform.runLater(() -> {
      this.cancelButton.setDisable(true);

      this.taskDescription.setText("");
      this.progressMinor.setProgress(0.0);
      this.progressMajor.setProgress(0.0);
    });
  }

  private void onTaskStartedEvent(
    final CGXControllerEventTaskStarted event)
  {
    Platform.runLater(() -> {
      this.cancelButton.setDisable(false);

      this.taskDescription.setText(event.description());
      this.progressMinor.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
      this.progressMajor.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    });
  }
}
