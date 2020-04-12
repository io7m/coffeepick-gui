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

import com.io7m.coffeepick.gui.controller.CGXControllerTaskType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;

import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 * Cells that appear in task lists.
 */

public final class CGXControllerTaskListCell
  extends ListCell<CGXControllerTaskType<?>>
{
  /**
   * Construct a task list cell.
   */

  public CGXControllerTaskListCell()
  {

  }

  @Override
  protected void updateItem(
    final CGXControllerTaskType<?> item,
    final boolean empty)
  {
    super.updateItem(item, empty);
    if (item == null || empty) {
      this.setText(null);
      this.setGraphic(null);
      return;
    }

    switch (item.status().value()) {
      case TASK_QUEUED: {
        this.setGraphic(CGXImages.iconOf("taskQueued16.png"));
        break;
      }
      case TASK_SUCCEEDED: {
        this.setGraphic(CGXImages.iconOf("taskSucceeded16.png"));
        break;
      }
      case TASK_FAILED: {
        this.setGraphic(CGXImages.iconOf("taskFailed16.png"));
        break;
      }
      case TASK_CANCELLED: {
        this.setGraphic(CGXImages.iconOf("taskCancelled16.png"));
        break;
      }
      case TASK_RUNNING: {
        final var progress = new ProgressIndicator(INDETERMINATE_PROGRESS);
        progress.setPrefWidth(16.0);
        progress.setPrefHeight(16.0);
        this.setGraphic(progress);
        break;
      }
    }

    this.setText(item.description());
  }
}
