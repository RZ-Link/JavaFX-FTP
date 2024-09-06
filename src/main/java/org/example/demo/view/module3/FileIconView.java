package org.example.demo.view.module3;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import de.saxsys.mvvmfx.FxmlView;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.example.demo.entity.FileEntity;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class FileIconView implements FxmlView<FileIconViewModel>, Initializable {
    public TableView<FileEntity> tableView;
    public TableColumn<FileEntity, String> fileNameColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        fileNameColumn.setCellFactory(column -> {
                    return new TableCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {

                            super.updateItem(item, empty);
                            if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                                setText(null);
                                setGraphic(null);
                            } else {

                                ImageView imageView = new ImageView();
                                imageView.setFitWidth(24);
                                imageView.setFitHeight(24);

                                if (getTableRow().getItem().getIsDirectory()) {
                                    imageView.setImage(new Image("/org/example/demo/image/places/folder-paleorange.png"));
                                } else {
                                    String mimeType = FileUtil.getMimeType(getTableRow().getItem().getFileName());
                                    if (StrUtil.isBlank(mimeType)) {
                                        mimeType = "text-plain.png";
                                    } else {
                                        mimeType = mimeType.replaceAll("/", "-") + ".png";
                                    }
                                    try {
                                        imageView.setImage(new Image("/org/example/demo/image/mimetypes/" + mimeType));
                                    } catch (Exception e) {
                                        imageView.setImage(new Image("/org/example/demo/image/mimetypes/text-plain.png"));
                                    }
                                }

                                HBox box = new HBox(imageView, new Label(item));
                                box.setAlignment(Pos.CENTER_LEFT);
                                box.setSpacing(5.0);
                                setGraphic(box);
                            }
                        }
                    };
                }
        );

        List<FileEntity> fileEntityList = new ArrayList<>();
        try {
            List<Path> filePathList = Files.list(Paths.get(System.getProperty("user.home"))).toList();
            for (Path filePath : filePathList) {
                File file = filePath.toFile();
                FileEntity fileEntity = new FileEntity();
                fileEntity.setFileName(file.getName());
                fileEntity.setIsDirectory(file.isDirectory());
                fileEntityList.add(fileEntity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        tableView.setItems(FXCollections.observableArrayList(fileEntityList));
    }
}
