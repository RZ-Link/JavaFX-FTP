package org.example.demo.view.feedback;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

// 确认对话框
public class ConfirmDialog {

    @FXML
    private Label messageLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    private void initialize(String message, Runnable confirmAction, Stage stage) {
        // 设置内容
        messageLabel.setText(message);

        confirmButton.setOnAction(e -> {
            confirmAction.run();
            stage.close();
        });
        cancelButton.setOnAction(e -> {
            stage.close();
        });
    }

    /**
     * @param title         标题
     * @param message       内容
     * @param confirmAction 确认执行的方法
     */
    public static void create(String title, String message, Runnable confirmAction) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(ConfirmDialog.class.getResource("/org/example/demo/view/feedback/ConfirmDialog.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BorderPane root = loader.getRoot();
        ConfirmDialog controller = loader.getController();
        controller.initialize(message, confirmAction, stage);
        // 设置标题
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }


}

