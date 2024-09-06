package org.example.demo.view.module3;

import cn.hutool.core.util.StrUtil;
import de.saxsys.mvvmfx.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.text.Font;
import org.example.demo.DemoApplication;

import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ReadFontView implements FxmlView<ReadFontViewModel>, Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void onDaoLiTiButtonClick(ActionEvent actionEvent) {

        Font font = Font.loadFont(getClass().getResourceAsStream("/org/example/demo/ttf/AlimamaDaoLiTi.ttf"), 0);

        String css = StrUtil.format(".root{-fx-font-family:\"{}\";}", font.getFamily());

        DemoApplication.stage.getScene().getRoot().getStylesheets().removeIf(uri -> uri.startsWith("data:text/css"));
        DemoApplication.stage.getScene().getRoot().getStylesheets().add(
                "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(UTF_8))
        );
        // DataURL介绍https://developer.mozilla.org/zh-CN/docs/Web/URI/Schemes/data
    }

    public void onDongFangDaKaiButtonClick(ActionEvent actionEvent) {
        Font font = Font.loadFont(getClass().getResourceAsStream("/org/example/demo/ttf/AlimamaDongFangDaKai-Regular.ttf"), 0);

        String css = StrUtil.format(".root{-fx-font-family:\"{}\";}", font.getFamily());

        DemoApplication.stage.getScene().getRoot().getStylesheets().removeIf(uri -> uri.startsWith("data:text/css"));
        DemoApplication.stage.getScene().getRoot().getStylesheets().add(
                "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(UTF_8))
        );
    }

    public void onDefaultButtonClick(ActionEvent actionEvent) {
        DemoApplication.stage.getScene().getRoot().getStylesheets().removeIf(uri -> uri.startsWith("data:text/css"));
    }
}
