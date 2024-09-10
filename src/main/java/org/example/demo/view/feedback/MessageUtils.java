package org.example.demo.view.feedback;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.example.demo.DemoApplication;

import java.util.ArrayList;

public class MessageUtils {

    private static final ArrayList<Popup> stack = new ArrayList<>();

    // 弹出成功消息
    public static void success(String message) {
        open(message, "#f0f9eb",
                "#e1f3d8",
                "/org/example/demo/image/message/成功.png",
                "#67c23a");
    }

    // 弹出错误消息
    public static void error(String message) {
        open(message, "#fef0f0",
                "#fde2e2",
                "/org/example/demo/image/message/错误.png",
                "#f56c6c");
    }

    private static void open(String message, String backgroundColor, String borderColor, String iconUrl, String textFill) {
        Platform.runLater(() -> {
            // 创建消息
            Popup popup = new Popup();

            HBox hbox = new HBox();
            hbox.setPadding(new Insets(5));
            hbox.setAlignment(Pos.CENTER);
            hbox.setSpacing(5);
            hbox.setMinWidth(0);
            hbox.setMaxWidth(500);
            hbox.setStyle(StrUtil.format("-fx-background-color: {}; -fx-background-radius: 4px; -fx-border-color: {}; -fx-border-radius: 4px; -fx-border-width: 1px;", backgroundColor, borderColor));
            // 消息图标
            ImageView icon = new ImageView(iconUrl);
            icon.setFitWidth(15);
            icon.setFitHeight(15);
            // 消息内容
            Label content = new Label(message);
            content.setWrapText(true);
            content.setTextFill(Paint.valueOf(textFill));

            hbox.getChildren().addAll(icon, content);

            popup.getContent().add(hbox);

            // 水平居中
            hbox.widthProperty().addListener((obs, old, val) -> {
                popup.setX(DemoApplication.stage.getX() + (DemoApplication.stage.getWidth() - popup.getWidth()) / 2);
            });
            // 垂直偏移
            synchronized (MessageUtils.class) {
                if (stack.isEmpty()) {
                    popup.setY(DemoApplication.stage.getY() + 100);
                } else {
                    var previousPopup = stack.get(stack.size() - 1);
                    popup.setY(previousPopup.getY() + previousPopup.getHeight() + 5);
                }
            }

            // 创建淡入效果
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), hbox);
            fadeIn.setFromValue(0.0); // 完全透明开始
            fadeIn.setToValue(1.0);   // 渐变完全不透明

            // 弹出消息
            synchronized (MessageUtils.class) {
                stack.add(popup);
            }
            popup.show(DemoApplication.stage);
            fadeIn.play();

            // 消息3秒后自动消失
            ThreadUtil.execute(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {

                } finally {
                    Platform.runLater(() -> {
                        // 创建淡出效果
                        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), hbox);
                        fadeOut.setFromValue(1.0); // 完全不透明开始
                        fadeOut.setToValue(0.0);   // 渐变完全透明
                        fadeOut.setOnFinished(event -> {
                            synchronized (MessageUtils.class) {
                                stack.remove(popup);
                            }
                            popup.hide();
                        });
                        fadeOut.play();
                    });
                }
            });

        });
    }
}


