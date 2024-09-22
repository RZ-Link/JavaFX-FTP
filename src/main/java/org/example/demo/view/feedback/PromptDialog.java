package org.example.demo.view.feedback;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

// 提交内容对话框
public class PromptDialog {

    public Label messageLabel;

    public TextField inputTextField;

    public Button confirmButton;

    public Button cancelButton;

    private void initialize(String message, Consumer<String> confirmAction, Stage stage) {
        // 设置内容
        messageLabel.setText(message);

        confirmButton.setOnAction(e -> {
            confirmAction.accept(inputTextField.getText());
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
    public static void create(String title, String message, Consumer<String> confirmAction) {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(PromptDialog.class.getResource("/org/example/demo/view/feedback/PromptDialog.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BorderPane root = loader.getRoot();
        PromptDialog controller = loader.getController();
        controller.initialize(message, confirmAction, stage);
        // 设置标题
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }
}
