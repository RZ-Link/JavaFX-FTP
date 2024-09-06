package org.example.demo.view.module4;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class TipVBox extends VBox {

    private final HBox controlBox;
    private final Label tipLabel;

    public TipVBox() {
        controlBox = new HBox();
        tipLabel = new Label();
        tipLabel.setVisible(false);
        tipLabel.setTextFill(Color.RED);

        this.getChildren().add(controlBox);
        this.getChildren().add(tipLabel);
        this.setSpacing(2);
        this.setAlignment(Pos.CENTER_LEFT);
    }

    public Node getControl() {
        if (this.controlBox.getChildren().isEmpty()) {
            return null;
        } else {
            return this.controlBox.getChildren().get(0);
        }
    }

    public void setControl(Node control) {
        this.controlBox.getChildren().setAll(control);
    }

    public void showTip(String tip) {
        tipLabel.setText(tip);
        tipLabel.setVisible(true);
    }

    public void hideTip() {
        tipLabel.setText(null);
        tipLabel.setVisible(false);
    }

}
