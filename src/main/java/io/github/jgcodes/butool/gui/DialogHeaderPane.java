package io.github.jgcodes.butool.gui;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class DialogHeaderPane extends GridPane {
  private final StringProperty headerText;
  private final ObjectProperty<Node> graphic;

  private final Label headerLabel;

  public DialogHeaderPane(String text) {
    super();

    getStyleClass().setAll("header-panel");

    headerText = new SimpleStringProperty(text) {
      @Override
      protected void invalidated() {
        update();
      }
    };
    graphic = new SimpleObjectProperty<>() {
      @Override
      protected void invalidated() {
        update();
      }
    };

    headerLabel = new Label(text);
    headerLabel.textProperty().bind(headerText);

    /* Column Constraints */ {
      ColumnConstraints labelConstraints = new ColumnConstraints();
      labelConstraints.setFillWidth(true);
      labelConstraints.setHgrow(Priority.ALWAYS);

      ColumnConstraints graphicConstraints = new ColumnConstraints();
      graphicConstraints.setFillWidth(false);
      graphicConstraints.setHgrow(Priority.NEVER);

      getColumnConstraints().setAll(labelConstraints, graphicConstraints);
    }

    update();
    requestLayout();
  }

  public DialogHeaderPane() {
    this("");
  }

  /*
   * PROPERTIES
   */

  public String getHeaderText() {
    return headerText.get();
  }

  public void setHeaderText(String headerText) {
    this.headerText.set(headerText);
  }

  public StringProperty headerTextProperty() {
    return headerText;
  }

  public Node getGraphic() {
    return graphic.get();
  }

  public void setGraphic(Node graphic) {
    this.graphic.set(graphic);
  }

  public ObjectProperty<Node> graphicProperty() {
    return graphic;
  }

  /*
   * UI FUNCTIONS
   */

  private void update() {
    ObservableList<Node> children = DialogHeaderPane.this.getChildren();
    children.clear();

    this.add(headerLabel, 0, 0);
    if (graphic.get() != null) this.add(graphic.get(), 1, 0);
  }
}
