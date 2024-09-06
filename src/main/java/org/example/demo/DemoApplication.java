package org.example.demo;

import atlantafx.base.theme.PrimerLight;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.demo.view.ftp.FTPView;
import org.example.demo.view.ftp.FTPViewModel;

public class DemoApplication extends Application {

    public static Stage stage;

    @Override
    public void start(Stage stage) {

        DemoApplication.stage = stage;

        // 设置AtlantaFX主题
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        // 线程未捕获异常处理，对话框提示
        Thread.currentThread().setUncaughtExceptionHandler(new DefaultExceptionHandler());
        // 启动窗口
        final ViewTuple<FTPView, FTPViewModel> viewTuple = FluentViewLoader.fxmlView(FTPView.class).load();
        final Parent root = viewTuple.getView();
        Scene scene = new Scene(root, 1280, 720);
        stage.setTitle("Demo Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}