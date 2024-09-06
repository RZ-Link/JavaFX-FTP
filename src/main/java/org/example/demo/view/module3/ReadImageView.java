package org.example.demo.view.module3;

import de.saxsys.mvvmfx.FxmlView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class ReadImageView implements FxmlView<ReadImageViewModel>, Initializable {

    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Image image = new Image(getClass().getResourceAsStream("/org/example/demo/image/office.png"));
        imageView.setImage(image);
    }
}
