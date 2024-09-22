package org.example.demo.view.ftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import de.saxsys.mvvmfx.FxmlView;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.example.demo.DemoApplication;
import org.example.demo.view.feedback.MessageUtils;
import org.example.demo.view.feedback.PromptDialog;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public VBox bottomTabPane;
    public ObservableList<TransferTaskVO> transferTaskQueue;
    public TableView<TransferTaskVO> transferTaskQueueTableView;
    public TableColumn<TransferTaskVO, String> fileNameColumn;
    public TableColumn<TransferTaskVO, String> typeColumn;
    public TableColumn<TransferTaskVO, Long> progressColumn;
    public TableColumn<TransferTaskVO, String> fileSizeColumn;
    public TableColumn<TransferTaskVO, String> localFilePathColumn;
    public TableColumn<TransferTaskVO, String> remoteFilePathColumn;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeFtpClient();
        initializeLocal();
        initializeRemote();
        initializeBottom();
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
                    if (row.getItem() != null) {
                        TransferTaskVO transferTaskVO = new TransferTaskVO();
                        transferTaskVO.setFileName(row.getItem().getFileName());
                        transferTaskVO.setType(TransferTaskVO.UPLOAD);
                        transferTaskVO.setProgress(0L);
                        transferTaskVO.setFileSize(row.getItem().getFileSize());
                        transferTaskVO.setLocalFilePath(row.getItem().getFilePath());
                        if (remotePathLabel.getText().endsWith("/")) {
                            transferTaskVO.setRemoteFilePath(remotePathLabel.getText() + row.getItem().getFileName());
                        } else {
                            transferTaskVO.setRemoteFilePath(remotePathLabel.getText() + "/" + row.getItem().getFileName());
                        }
                        transferTaskVO.setStatus(TransferTaskVO.WAITING);
                        transferTaskQueue.add(transferTaskVO);
                    }
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
                    List<File> files = FileUtil.loopFiles(row.getItem().getFilePath());

                    String localPrefix = row.getItem().getFilePath();
                    if (localPrefix.endsWith(File.separator)) {
                        localPrefix = localPrefix.substring(0, localPrefix.length() - 1);
                    }

                    String remotePrefix = remotePathLabel.getText();
                    if (!remotePathLabel.getText().endsWith("/")) {
                        remotePrefix += "/";
                    }

                    for (File file : files) {
                        String remoteFilePath = remotePrefix + row.getItem().getFileName() + file.getAbsolutePath().substring(localPrefix.length()).replace(File.separator, "/");

                        TransferTaskVO transferTaskVO = new TransferTaskVO();
                        transferTaskVO.setFileName(file.getName());
                        transferTaskVO.setType(TransferTaskVO.UPLOAD);
                        transferTaskVO.setProgress(0L);
                        transferTaskVO.setFileSize(FileUtils.byteCountToDisplaySize(file.length()));
                        transferTaskVO.setLocalFilePath(file.getAbsolutePath());
                        transferTaskVO.setRemoteFilePath(remoteFilePath);
                        transferTaskVO.setStatus(TransferTaskVO.WAITING);
                        transferTaskQueue.add(transferTaskVO);
                    }

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

        String userHomePath = FileUtil.getUserHomePath();
        if (userHomePath.endsWith(File.separator)) {
            localPathLabel.setText(userHomePath.substring(0, userHomePath.length() - 1));
        } else {
            localPathLabel.setText(userHomePath);
        }

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
                    if (row.getItem() != null) {
                        TransferTaskVO transferTaskVO = new TransferTaskVO();
                        transferTaskVO.setFileName(row.getItem().getFileName());
                        transferTaskVO.setType(TransferTaskVO.DOWNLOAD);
                        transferTaskVO.setProgress(0L);
                        transferTaskVO.setFileSize(row.getItem().getFileSize());
                        if (localPathLabel.getText().endsWith(File.separator)) {
                            transferTaskVO.setLocalFilePath(localPathLabel.getText() + row.getItem().getFileName());
                        } else {
                            transferTaskVO.setLocalFilePath(localPathLabel.getText() + File.separator + row.getItem().getFileName());
                        }
                        transferTaskVO.setRemoteFilePath(row.getItem().getFilePath());
                        transferTaskVO.setStatus(TransferTaskVO.WAITING);
                        transferTaskQueue.add(transferTaskVO);
                    }
                });
                menuItem2.setOnAction((event) -> {
                    System.out.println("删除文件" + row.getItem().getFilePath());
                    try {
                        boolean done = ftpClient.deleteFile(row.getItem().getFilePath());
                        if (done) {
                            System.out.println("删除文件成功");
                        } else {
                            System.out.println("删除文件失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    refreshRemoteFileList();
                });
                menuItem3.setOnAction((event) -> {
                    System.out.println("创建目录");
                    PromptDialog.create("创建目录", "目录名", directoryName -> {
                        try {
                            String pathName = remotePathLabel.getText();
                            if (pathName.endsWith("/")) {
                                pathName += directoryName;
                            } else {
                                pathName += "/" + directoryName;
                            }
                            boolean done = ftpClient.makeDirectory(pathName);
                            if (done) {
                                System.out.println("创建目录成功");
                            } else {
                                System.out.println("创建目录失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("创建目录失败");
                        }
                        refreshRemoteFileList();
                    });
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
                    deleteDirectory(row.getItem().getFilePath());
                    refreshRemoteFileList();
                });
                menuItem3.setOnAction((event) -> {
                    System.out.println("创建目录");
                    PromptDialog.create("创建目录", "目录名", directoryName -> {
                        try {
                            String pathName = remotePathLabel.getText();
                            if (pathName.endsWith("/")) {
                                pathName += directoryName;
                            } else {
                                pathName += "/" + directoryName;
                            }
                            boolean done = ftpClient.makeDirectory(pathName);
                            if (done) {
                                System.out.println("创建目录成功");
                            } else {
                                System.out.println("创建目录失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("创建目录失败");
                        }
                        refreshRemoteFileList();
                    });
                });
                directoryMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);
            }

            ContextMenu tableMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("创建目录");
                menuItem1.setOnAction((event) -> {
                    System.out.println("创建目录");
                    PromptDialog.create("创建目录", "目录名", directoryName -> {
                        try {
                            String pathName = remotePathLabel.getText();
                            if (pathName.endsWith("/")) {
                                pathName += directoryName;
                            } else {
                                pathName += "/" + directoryName;
                            }
                            boolean done = ftpClient.makeDirectory(pathName);
                            if (done) {
                                System.out.println("创建目录成功");
                            } else {
                                System.out.println("创建目录失败");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("创建目录失败");
                        }
                        refreshRemoteFileList();
                    });
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
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.connect("192.168.153.130", 2121);
            ftpClient.login("admin", "admin");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeBottom() {
        bottomTabPane.prefHeightProperty().bind(DemoApplication.stage.heightProperty().multiply(0.35));
        {
            transferTaskQueue = FXCollections.observableArrayList();
            transferTaskQueueTableView.setItems(transferTaskQueue);

            fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
            fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
            localFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("localFilePath"));
            remoteFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("remoteFilePath"));

            transferTaskQueue.addListener((ListChangeListener<TransferTaskVO>) c -> {
                synchronized (FTPView.class) {
                    if (!transferTaskQueue.isEmpty()) {
                        TransferTaskVO transferTaskVO = transferTaskQueue.get(0);
                        if (Objects.equals(transferTaskVO.getStatus(), TransferTaskVO.WAITING)) {
                            transferTaskVO.setStatus(TransferTaskVO.RUNNING);
                            executorService.submit(() -> {
                                if (Objects.equals(transferTaskVO.getType(), TransferTaskVO.UPLOAD)) {
                                    var inputStream = FileUtil.getInputStream(transferTaskVO.getLocalFilePath());

                                    try {
                                        boolean makeDirectoryDone = true;

                                        synchronized (FTPView.class) {
                                            Path remoteFilePath = Paths.get(transferTaskVO.getRemoteFilePath());
                                            String remoteFileParentPath = remoteFilePath.getParent().toString().replace(File.separator, "/");
                                            String[] parts = remoteFileParentPath.split("/");
                                            String path = "";
                                            for (String part : parts) {
                                                path += "/" + part;
                                                if (!ftpClient.changeWorkingDirectory(path)) {
                                                    boolean done = ftpClient.makeDirectory(path);
                                                    if (!done) {
                                                        makeDirectoryDone = false;
                                                        break;
                                                    }
                                                }
                                            }
                                        }


                                        if (makeDirectoryDone) {
                                            boolean done = ftpClient.storeFile(transferTaskVO.getRemoteFilePath(), inputStream);
                                            if (done) {
                                                transferTaskVO.setStatus(TransferTaskVO.SUCCESS);
                                            } else {
                                                transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                            }
                                        } else {
                                            transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                    }

                                    try {
                                        inputStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    refreshRemoteFileList();

                                } else if (Objects.equals(transferTaskVO.getType(), TransferTaskVO.DOWNLOAD)) {
                                    var outputStream = FileUtil.getOutputStream(transferTaskVO.getLocalFilePath());

                                    try {
                                        boolean done = ftpClient.retrieveFile(transferTaskVO.getRemoteFilePath(), outputStream);
                                        if (done) {
                                            transferTaskVO.setStatus(TransferTaskVO.SUCCESS);
                                        } else {
                                            transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                    }

                                    try {
                                        outputStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    refreshLocalFileList();
                                }

                                transferTaskQueue.remove(transferTaskVO);
                            });
                        }
                    }
                }
            });

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


    public void deleteDirectory(String remoteDirectoryPath) {
        synchronized (FTPView.class) {
            try {
                ftpClient.changeWorkingDirectory("/");

                FTPFile[] files = ftpClient.listFiles(remoteDirectoryPath);

                for (FTPFile file : files) {

                    String pathName = remoteDirectoryPath;
                    if (pathName.endsWith("/")) {
                        pathName += file.getName();
                    } else {
                        pathName += "/" + file.getName();
                    }

                    if (file.isDirectory()) {
                        deleteDirectory(pathName);
                    } else {
                        boolean done = ftpClient.deleteFile(pathName);
                        if (done) {
                            System.out.println("删除文件成功");
                        } else {
                            System.out.println("删除文件失败");
                        }
                    }
                }
                boolean done = ftpClient.removeDirectory(remoteDirectoryPath);
                if (done) {
                    System.out.println("删除目录成功");
                } else {
                    System.out.println("删除目录失败");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
