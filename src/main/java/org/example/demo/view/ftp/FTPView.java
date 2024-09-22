package org.example.demo.view.ftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import de.saxsys.mvvmfx.FxmlView;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.example.demo.DemoApplication;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class FTPView implements FxmlView<FTPViewModel>, Initializable {
    public VBox localBox;
    public HBox localFileListBox;
    public Label localPathLabel;
    public TableView<FileVO> localFileListTableView;
    public TableColumn<FileVO, String> localFileNameColumn;
    public TableColumn<FileVO, String> localFileSizeColumn;
    public TableColumn<FileVO, String> localFileLastModifiedTimeColumn;

    public VBox remoteBox;
    public HBox remoteFileListBox;
    public Label remotePathLabel;
    public TableView<FileVO> remoteFileListTableView;
    public TableColumn<FileVO, String> remoteFileNameColumn;
    public TableColumn<FileVO, String> remoteFileSizeColumn;
    public TableColumn<FileVO, String> remoteFileLastModifiedTimeColumn;

    public FTPClient ftpClient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeFtpClient();
        initializeLocal();
        initializeRemote();
    }

    public void initializeLocal() {

        localBox.prefWidthProperty().bind(DemoApplication.stage.widthProperty().multiply(0.5).subtract(10));
        localFileListBox.prefHeightProperty().bind(DemoApplication.stage.heightProperty().multiply(0.5));

        localFileListTableView.setRowFactory(tableView -> {
            TableRow<FileVO> row = new TableRow<>();

            ContextMenu fileMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("上传文件");
                menuItem1.setOnAction((event) -> {
                    System.out.println("上传文件" + row.getItem().getFilePath());
                });
                fileMenu.getItems().addAll(menuItem1);
            }

            ContextMenu directoryMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("打开目录");
                MenuItem menuItem2 = new MenuItem("上传目录");
                menuItem1.setOnAction((event) -> {
                    System.out.println("打开目录" + row.getItem().getFilePath());
                });
                menuItem2.setOnAction((event) -> {
                    System.out.println("上传目录" + row.getItem().getFilePath());
                });
                directoryMenu.getItems().addAll(menuItem1, menuItem2);
            }

            row.setOnMouseClicked(event -> {
                fileMenu.hide();
                directoryMenu.hide();

                if (Objects.equals(event.getButton(), MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    if (row.getItem() != null && row.getItem().getIsDirectory()) {

                        if (localPathLabel.getText().endsWith(File.separator)) {
                            localPathLabel.setText(localPathLabel.getText() + row.getItem().getFileName());
                        } else {
                            localPathLabel.setText(localPathLabel.getText() + File.separator + row.getItem().getFileName());
                        }

                        refreshLocalFileList();
                    }
                }

                if (Objects.equals(event.getButton(), MouseButton.SECONDARY)) {
                    if (row.getItem() != null) {
                        if (row.getItem().getIsDirectory()) {
                            directoryMenu.show(row, event.getScreenX(), event.getScreenY());
                        } else {
                            fileMenu.show(row, event.getScreenX(), event.getScreenY());
                        }
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

        localPathLabel.setText(FileUtil.getUserHomePath());

        refreshLocalFileList();
    }

    public void initializeRemote() {

        remoteBox.prefWidthProperty().bind(DemoApplication.stage.widthProperty().multiply(0.5).subtract(10));
        remoteFileListBox.prefHeightProperty().bind(DemoApplication.stage.heightProperty().multiply(0.5));

        remoteFileListTableView.setRowFactory(tableView -> {
            TableRow<FileVO> row = new TableRow<>();

            ContextMenu fileMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("下载文件");
                MenuItem menuItem2 = new MenuItem("删除文件");
                MenuItem menuItem3 = new MenuItem("创建目录");
                menuItem1.setOnAction((event) -> {
                    System.out.println("下载文件" + row.getItem().getFilePath());
                });
                menuItem2.setOnAction((event) -> {
                    System.out.println("删除文件" + row.getItem().getFilePath());
                });
                menuItem3.setOnAction((event) -> {
                    System.out.println("创建目录");
                });
                fileMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);
            }

            ContextMenu directoryMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("下载目录");
                MenuItem menuItem2 = new MenuItem("删除目录");
                MenuItem menuItem3 = new MenuItem("创建目录");
                menuItem1.setOnAction((event) -> {
                    System.out.println("下载目录" + row.getItem().getFilePath());
                });
                menuItem2.setOnAction((event) -> {
                    System.out.println("删除目录" + row.getItem().getFilePath());
                });
                menuItem3.setOnAction((event) -> {
                    System.out.println("创建目录");
                });
                directoryMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);
            }

            ContextMenu tableMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("创建目录");
                menuItem1.setOnAction((event) -> {
                    System.out.println("创建目录");
                });
                tableMenu.getItems().addAll(menuItem1);
            }

            row.setOnMouseClicked(event -> {
                fileMenu.hide();
                directoryMenu.hide();
                tableMenu.hide();

                if (Objects.equals(event.getButton(), MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    if (row.getItem() != null && row.getItem().getIsDirectory()) {

                        if (remotePathLabel.getText().endsWith("/")) {
                            remotePathLabel.setText(remotePathLabel.getText() + row.getItem().getFileName());
                        } else {
                            remotePathLabel.setText(remotePathLabel.getText() + "/" + row.getItem().getFileName());
                        }

                        refreshRemoteFileList();
                    }
                }
                if (Objects.equals(event.getButton(), MouseButton.SECONDARY)) {
                    if (row.getItem() != null) {
                        if (row.getItem().getIsDirectory()) {
                            directoryMenu.show(row, event.getScreenX(), event.getScreenY());
                        } else {
                            fileMenu.show(row, event.getScreenX(), event.getScreenY());
                        }
                    } else {
                        tableMenu.show(row, event.getScreenX(), event.getScreenY());
                    }
                }
            });
            return row;
        });

        remoteFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        remoteFileNameColumn.setCellFactory(column -> {
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
        remoteFileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        remoteFileLastModifiedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("fileLastModifiedTime"));

        remotePathLabel.setText("/");

        refreshRemoteFileList();
    }

    public void initializeFtpClient() {
        try {
            ftpClient = new FTPClient();
            ftpClient.connect("192.168.153.130", 2121);
            ftpClient.login("admin", "admin");
            ftpClient.enterLocalPassiveMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLocalBackButtonClick(ActionEvent actionEvent) {
        Path localPath = Paths.get(localPathLabel.getText());
        if (localPath.getParent() == null) {
            return;
        }
        localPathLabel.setText(localPath.getParent().toString());

        refreshLocalFileList();
    }

    public void onRemoteBackButtonClick(ActionEvent actionEvent) {
        Path remotePath = Paths.get(remotePathLabel.getText());
        if (remotePath.getParent() == null) {
            return;
        }
        remotePathLabel.setText(remotePath.getParent().toString().replace(File.separator, "/"));

        refreshRemoteFileList();
    }

    public void refreshLocalFileList() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<FileVO> fileVOList = new ArrayList<>();
        try {
            for (File file : FileUtil.ls(localPathLabel.getText())) {
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

    public void refreshRemoteFileList() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<FileVO> fileVOList = new ArrayList<>();
        try {
            for (FTPFile file : ftpClient.mlistDir(remotePathLabel.getText())) {
                FileVO fileVO = new FileVO();
                fileVO.setFileName(file.getName());
                fileVO.setFilePath(remotePathLabel.getText() + file.getName());
                if (!file.isDirectory()) {
                    fileVO.setFileSize(FileUtils.byteCountToDisplaySize(file.getSize()));
                }
                fileVO.setFileLastModifiedTime(sdf.format(file.getTimestamp().getTime()));
                fileVO.setIsDirectory(file.isDirectory());
                fileVOList.add(fileVO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        remoteFileListTableView.setItems(FXCollections.observableArrayList(fileVOList));
    }
}
