package org.example.demo.view.ftp;

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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;
import org.example.demo.DemoApplication;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class FTPView implements FxmlView<FTPViewModel>, Initializable {
    public VBox localBox;
    public HBox localFileListBox;
    public TableView<FileVO> localFileListTableView;
    public TableColumn<FileVO, String> localFileNameColumn;
    public TableColumn<FileVO, String> localFileSizeColumn;
    public TableColumn<FileVO, String> localFileLastModifiedTimeColumn;

    public VBox remoteBox;
    public HBox remoteFileListBox;
    public TableView<FileVO> remoteFileListTableView;
    public TableColumn<FileVO, String> remoteFileNameColumn;
    public TableColumn<FileVO, String> remoteFileSizeColumn;
    public TableColumn<FileVO, String> remoteFileLastModifiedTimeColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeLocal();
        initializeRemote();
    }

    public void initializeLocal() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        localBox.prefWidthProperty().bind(DemoApplication.stage.widthProperty().multiply(0.5).subtract(10));
        localFileListBox.prefHeightProperty().bind(DemoApplication.stage.heightProperty().multiply(0.5));

        localFileListTableView.setRowFactory(tableView -> {
            TableRow<FileVO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (Objects.equals(event.getButton(), MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    if (row.getItem() != null && row.getItem().getIsDirectory()) {
                        List<FileVO> fileVOList = new ArrayList<>();
                        File directory = FileUtil.file(row.getItem().getFilePath());
                        File[] files = directory.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                FileVO fileVO = new FileVO();
                                fileVO.setFileName(file.getName());
                                fileVO.setFilePath(file.getPath());
                                if (!file.isDirectory()) {
                                    fileVO.setFileSize(FileUtils.byteCountToDisplaySize(file.length()));
                                }
                                fileVO.setFileLastModifiedTime(sdf.format(new Date(file.lastModified())));
                                fileVO.setIsDirectory(file.isDirectory());
                                fileVOList.add(fileVO);
                            }
                        }
                        localFileListTableView.setItems(FXCollections.observableArrayList(fileVOList));
                    }
                }
            });
            return row;
        });

        localFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        localFileNameColumn.setCellFactory(column -> {
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
        localFileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        localFileLastModifiedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("fileLastModifiedTime"));

        List<FileVO> fileVOList = new ArrayList<>();
        try {
            for (File file : FileUtil.getUserHomeDir().listFiles()) {
                FileVO fileVO = new FileVO();
                fileVO.setFileName(file.getName());
                fileVO.setFilePath(file.getPath());
                if (!file.isDirectory()) {
                    fileVO.setFileSize(FileUtils.byteCountToDisplaySize(file.length()));
                }
                fileVO.setFileLastModifiedTime(sdf.format(new Date(file.lastModified())));
                fileVO.setIsDirectory(file.isDirectory());
                fileVOList.add(fileVO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        localFileListTableView.setItems(FXCollections.observableArrayList(fileVOList));

    }

    public void initializeRemote() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        remoteBox.prefWidthProperty().bind(DemoApplication.stage.widthProperty().multiply(0.5).subtract(10));
        remoteFileListBox.prefHeightProperty().bind(DemoApplication.stage.heightProperty().multiply(0.5));

    }
}
