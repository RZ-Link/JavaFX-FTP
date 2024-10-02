package org.example.demo.view.ftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import de.saxsys.mvvmfx.FxmlView;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.example.demo.DemoApplication;
import org.example.demo.view.feedback.PromptDialog;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class FTPView implements FxmlView<FTPViewModel>, Initializable {
    public VBox localBox;
    public HBox localFileListBox;
    public Label localPathLabel; // 需要考虑兼容D:\和D:\JDK场景
    public TableView<FileVO> localFileListTableView;
    public TableColumn<FileVO, String> localFileNameColumn;
    public TableColumn<FileVO, String> localFileDisplaySizeColumn;
    public TableColumn<FileVO, String> localFileLastModifiedTimeColumn;

    public VBox remoteBox;
    public HBox remoteFileListBox;
    public Label remotePathLabel; // 需要考虑兼容/和/home/root场景
    public TableView<FileVO> remoteFileListTableView;
    public TableColumn<FileVO, String> remoteFileNameColumn;
    public TableColumn<FileVO, String> remoteFileDisplaySizeColumn;
    public TableColumn<FileVO, String> remoteFileLastModifiedTimeColumn;

    public VBox bottomTabPane;
    public ObservableList<TransferTaskVO> transferTaskQueue;
    public TableView<TransferTaskVO> transferTaskQueueTableView;
    public TableColumn<TransferTaskVO, String> fileNameColumn;
    public TableColumn<TransferTaskVO, String> typeColumn;
    public TableColumn<TransferTaskVO, String> progressColumn;
    public TableColumn<TransferTaskVO, String> fileDisplaySizeColumn;
    public TableColumn<TransferTaskVO, String> localFilePathColumn;
    public TableColumn<TransferTaskVO, String> remoteFilePathColumn;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeLocal();
        initializeRemote();
        initializeBottom();
    }

    /**
     * 本地站点初始化
     */
    public void initializeLocal() {
        // 设置宽高
        localBox.prefWidthProperty().bind(DemoApplication.stage.widthProperty().multiply(0.5).subtract(10));
        localFileListBox.prefHeightProperty().bind(DemoApplication.stage.heightProperty().multiply(0.5));

        localFileListTableView.setRowFactory(tableView -> {
            TableRow<FileVO> row = new TableRow<>();

            ContextMenu fileMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("上传文件");
                menuItem1.setOnAction((event) -> {
                    if (row.getItem() != null) {
                        TransferTaskVO transferTaskVO = new TransferTaskVO();
                        transferTaskVO.setFileName(row.getItem().getFileName());
                        transferTaskVO.setType(TransferTaskVO.UPLOAD);
                        transferTaskVO.setFileByteCount(row.getItem().getFileByteCount());
                        transferTaskVO.setBytesTransferred(0L);
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
                MenuItem menuItem1 = new MenuItem("上传目录");
                menuItem1.setOnAction((event) -> {
                    // 假设
                    // row.getItem().getFilePath()=D:\JDK
                    // remotePathLabel=/home/root
                    List<File> files = FileUtil.loopFiles(row.getItem().getFilePath());
                    if (files == null || files.isEmpty()) {
                        return;
                    }
                    // localPrefix=D:\JDK
                    String localPrefix = row.getItem().getFilePath();
                    // remotePrefix=/home/root/
                    String remotePrefix = remotePathLabel.getText();
                    if (!remotePrefix.endsWith("/")) {
                        remotePrefix += "/";
                    }

                    for (File file : files) {
                        // 拼接远程文件路径/home/root/ + JDK + /OpenJDK8U-jdk_x64_windows_hotspot_8u422b05/jdk8u422-b05/bin/java.exe
                        String remoteFilePath = remotePrefix + row.getItem().getFileName() + file.getAbsolutePath().substring(localPrefix.length()).replace(File.separator, "/");

                        TransferTaskVO transferTaskVO = new TransferTaskVO();
                        transferTaskVO.setFileName(file.getName());
                        transferTaskVO.setType(TransferTaskVO.UPLOAD);
                        transferTaskVO.setFileByteCount(file.length());
                        transferTaskVO.setBytesTransferred(0L);
                        transferTaskVO.setLocalFilePath(file.getAbsolutePath());
                        transferTaskVO.setRemoteFilePath(remoteFilePath);
                        transferTaskVO.setStatus(TransferTaskVO.WAITING);
                        transferTaskQueue.add(transferTaskVO);
                    }

                });
                directoryMenu.getItems().addAll(menuItem1);
            }

            row.setOnMouseClicked(event -> {
                fileMenu.hide();
                directoryMenu.hide();

                // 双击左键，进入目录
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

                // 单机右键，打开菜单
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

        // 本地站点列初始化
        initializeLocalColumn();

        // 本地站点数据初始化
        localPathLabel.setText(FileUtil.getUserHomePath());
        refreshLocalFileList();
    }

    /**
     * 远程站点初始化
     */
    public void initializeRemote() {
        // 设置宽高
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
                    if (row.getItem() != null) {
                        TransferTaskVO transferTaskVO = new TransferTaskVO();
                        transferTaskVO.setFileName(row.getItem().getFileName());
                        transferTaskVO.setType(TransferTaskVO.DOWNLOAD);
                        transferTaskVO.setFileByteCount(row.getItem().getFileByteCount());
                        transferTaskVO.setBytesTransferred(0L);
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
                    var ftpClient = connectAndLogin();
                    boolean done = FTPService.deleteFile(ftpClient,
                            row.getItem().getFilePath());
                    logoutAndDisconnect(ftpClient);

                    if (done) {
                        System.out.println("删除文件成功");
                    } else {
                        System.out.println("删除文件失败");
                    }

                    refreshRemoteFileList();
                });
                menuItem3.setOnAction((event) -> {
                    makeRemoteDirectory();
                });
                fileMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);
            }

            ContextMenu directoryMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("下载目录");
                MenuItem menuItem2 = new MenuItem("删除目录");
                MenuItem menuItem3 = new MenuItem("创建目录");
                menuItem1.setOnAction((event) -> {
                    // 假设
                    // row.getItem().getFilePath()=/home/root/apache-ftpserver-1.2.0
                    // localPathLabel.getText()=D:\
                    var ftpClient = connectAndLogin();
                    List<Pair<FTPFile, String>> remoteFilePathList = FTPService.loopFiles(ftpClient,
                            row.getItem().getFilePath());
                    logoutAndDisconnect(ftpClient);
                    if (remoteFilePathList == null || remoteFilePathList.isEmpty()) {
                        return;
                    }
                    // localPrefix=D:\
                    String localPrefix = localPathLabel.getText();
                    if (!localPrefix.endsWith(File.separator)) {
                        localPrefix += File.separator;
                    }
                    // remotePrefix=/home/root/apache-ftpserver-1.2.0
                    String remotePrefix = row.getItem().getFilePath();

                    for (Pair<FTPFile, String> pair : remoteFilePathList) {
                        FTPFile file = pair.getKey();
                        String remoteFilePath = pair.getValue();
                        // 拼接本地文件路径D:\ + apache-ftpserver-1.2.0 + \bin\ftpd.exe
                        String localFilePath = localPrefix + row.getItem().getFileName() + remoteFilePath.substring(remotePrefix.length()).replace("/", File.separator);

                        TransferTaskVO transferTaskVO = new TransferTaskVO();
                        transferTaskVO.setFileName(file.getName());
                        transferTaskVO.setType(TransferTaskVO.DOWNLOAD);
                        transferTaskVO.setFileByteCount(file.getSize());
                        transferTaskVO.setBytesTransferred(0L);
                        transferTaskVO.setLocalFilePath(localFilePath);
                        transferTaskVO.setRemoteFilePath(remoteFilePath);
                        transferTaskVO.setStatus(TransferTaskVO.WAITING);
                        transferTaskQueue.add(transferTaskVO);
                    }
                });
                menuItem2.setOnAction((event) -> {
                    var ftpClient = connectAndLogin();
                    boolean done = FTPService.deleteDirectory(ftpClient,
                            row.getItem().getFilePath());
                    logoutAndDisconnect(ftpClient);

                    if (done) {
                        System.out.println("删除目录成功");
                    } else {
                        System.out.println("删除目录失败");
                    }
                    refreshRemoteFileList();
                });
                menuItem3.setOnAction((event) -> {
                    makeRemoteDirectory();
                });
                directoryMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);
            }

            ContextMenu tableMenu = new ContextMenu();
            {
                MenuItem menuItem1 = new MenuItem("创建目录");
                menuItem1.setOnAction((event) -> {
                    makeRemoteDirectory();
                });
                tableMenu.getItems().addAll(menuItem1);
            }

            row.setOnMouseClicked(event -> {
                fileMenu.hide();
                directoryMenu.hide();
                tableMenu.hide();

                // 双击左键，进入目录
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

                // 单机右键，打开菜单
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

        // 远程站点列初始化
        initializeRemoteColumn();
        // 远程站点数据初始化
        remotePathLabel.setText("/");
        refreshRemoteFileList();
    }

    /**
     * 本地站点列初始化
     */
    public void initializeLocalColumn() {
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
                                        mimeType = mimeType.replace("/", "-") + ".png";
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
        localFileDisplaySizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileDisplaySize"));
        localFileLastModifiedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("fileLastModifiedTime"));
    }

    /**
     * 远程站点列初始化
     */
    public void initializeRemoteColumn() {
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
                                        mimeType = mimeType.replace("/", "-") + ".png";
                                    }
                                    try {
                                        imageView.setImage(new Image("/org/example/demo/image/mimetypes/" + mimeType));
                                    } catch (Exception e) {
                                        e.printStackTrace();
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
        remoteFileDisplaySizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileDisplaySize"));
        remoteFileLastModifiedTimeColumn.setCellValueFactory(new PropertyValueFactory<>("fileLastModifiedTime"));
    }

    /**
     * 远程站点创建目录
     */
    public void makeRemoteDirectory() {
        PromptDialog.create("创建目录", "目录名", directoryName -> {

            String pathName = remotePathLabel.getText();
            if (pathName.endsWith("/")) {
                pathName += directoryName;
            } else {
                pathName += "/" + directoryName;
            }

            var ftpClient = connectAndLogin();
            boolean done = FTPService.makeDirectory(ftpClient,
                    pathName);
            logoutAndDisconnect(ftpClient);

            if (done) {
                System.out.println("创建目录成功");
            } else {
                System.out.println("创建目录失败");
            }


            refreshRemoteFileList();
        });
    }

    /**
     * 传输队列 & 日志初始化
     */
    public void initializeBottom() {
        // 设置高度
        bottomTabPane.prefHeightProperty().bind(DemoApplication.stage.heightProperty().multiply(0.35));

        {
            transferTaskQueue = FXCollections.observableArrayList();
            transferTaskQueueTableView.setItems(transferTaskQueue);

            fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            progressColumn.setCellValueFactory(c -> new SimpleStringProperty(""));
            progressColumn.setCellFactory(column -> {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            HBox box = new HBox();
                            box.setAlignment(Pos.CENTER_LEFT);
                            box.setSpacing(1.0);

                            var progressBar = new ProgressBar(getTableRow().getItem().getBytesTransferred().doubleValue() / getTableRow().getItem().getFileByteCount().doubleValue());
                            box.getChildren().add(progressBar);
                            box.getChildren().add(new Label(Math.round(progressBar.getProgress() * 100) + "%"));

                            setGraphic(box);
                        }
                    }
                };
            });
            fileDisplaySizeColumn.setCellValueFactory(c -> new SimpleStringProperty(FileUtils.byteCountToDisplaySize(c.getValue().getFileByteCount())));
            localFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("localFilePath"));
            remoteFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("remoteFilePath"));

            transferTaskQueue.addListener((ListChangeListener<TransferTaskVO>) c -> {
                if (!transferTaskQueue.isEmpty()) {
                    TransferTaskVO transferTaskVO = transferTaskQueue.get(0);
                    if (Objects.equals(transferTaskVO.getStatus(), TransferTaskVO.WAITING)) {
                        transferTaskVO.setStatus(TransferTaskVO.RUNNING);
                        executorService.submit(() -> {
                            var ftpClient = connectAndLogin();

                            if (Objects.equals(transferTaskVO.getType(), TransferTaskVO.UPLOAD)) {
                                Path remoteFilePath = Paths.get(transferTaskVO.getRemoteFilePath());
                                String remoteFileParentPath = remoteFilePath.getParent().toString().replace(File.separator, "/");
                                boolean makeDirectoryDone = FTPService.makeDirectory(ftpClient,
                                        remoteFileParentPath);

                                if (makeDirectoryDone) {
                                    boolean done = FTPService.storeFile(ftpClient,
                                            transferTaskVO.getLocalFilePath(),
                                            transferTaskVO.getRemoteFilePath(),
                                            totalBytesRead -> {
                                                transferTaskVO.setBytesTransferred(totalBytesRead);
                                                transferTaskQueueTableView.refresh();
                                            });
                                    if (done) {
                                        transferTaskVO.setStatus(TransferTaskVO.SUCCESS);
                                    } else {
                                        transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                    }
                                } else {
                                    transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                }

                                refreshRemoteFileList();
                            } else if (Objects.equals(transferTaskVO.getType(), TransferTaskVO.DOWNLOAD)) {
                                File parentDirectory = FileUtil.mkParentDirs(transferTaskVO.getLocalFilePath());

                                if (parentDirectory != null) {
                                    boolean done = FTPService.retrieveFile(ftpClient,
                                            transferTaskVO.getLocalFilePath(),
                                            transferTaskVO.getRemoteFilePath(),
                                            totalBytesWrite -> {
                                                transferTaskVO.setBytesTransferred(totalBytesWrite);
                                                transferTaskQueueTableView.refresh();
                                            });
                                    if (done) {
                                        transferTaskVO.setStatus(TransferTaskVO.SUCCESS);
                                    } else {
                                        transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                    }
                                } else {
                                    transferTaskVO.setStatus(TransferTaskVO.FAIL);
                                }

                                refreshLocalFileList();
                            } else {
                                transferTaskVO.setStatus(TransferTaskVO.FAIL);
                            }

                            transferTaskQueue.remove(transferTaskVO);

                            logoutAndDisconnect(ftpClient);
                        });
                    }
                }
                transferTaskQueueTableView.refresh();
            });
        }
    }

    /**
     * 本地站点后退
     */
    public void onLocalBackButtonClick(ActionEvent actionEvent) {
        Path localPath = Paths.get(localPathLabel.getText());
        if (localPath.getParent() == null) {
            return;
        }
        localPathLabel.setText(localPath.getParent().toString());
        refreshLocalFileList();
    }

    /**
     * 远程站点后退
     */
    public void onRemoteBackButtonClick(ActionEvent actionEvent) {
        Path remotePath = Paths.get(remotePathLabel.getText());
        if (remotePath.getParent() == null) {
            return;
        }
        remotePathLabel.setText(remotePath.getParent().toString().replace(File.separator, "/"));
        refreshRemoteFileList();
    }

    /**
     * 刷新本地站点
     */
    public void refreshLocalFileList() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<FileVO> fileVOList = new ArrayList<>();

        var files = FileUtil.ls(localPathLabel.getText());

        if (files != null) {
            for (File file : files) {
                FileVO fileVO = new FileVO();
                fileVO.setFileName(file.getName());
                fileVO.setFilePath(file.getPath());
                if (!file.isDirectory()) {
                    fileVO.setFileByteCount(file.length());
                    fileVO.setFileDisplaySize(FileUtils.byteCountToDisplaySize(file.length()));
                }
                fileVO.setFileLastModifiedTime(sdf.format(new Date(file.lastModified())));
                fileVO.setIsDirectory(file.isDirectory());
                fileVOList.add(fileVO);
            }
        }

        localFileListTableView.setItems(FXCollections.observableArrayList(fileVOList));
    }

    /**
     * 刷新远程站点
     */
    public void refreshRemoteFileList() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<FileVO> fileVOList = new ArrayList<>();

        var ftpClient = connectAndLogin();
        var files = FTPService.mlistDir(ftpClient, remotePathLabel.getText());
        logoutAndDisconnect(ftpClient);

        if (files != null) {
            for (FTPFile file : files) {
                FileVO fileVO = new FileVO();
                fileVO.setFileName(file.getName());
                if (remotePathLabel.getText().endsWith("/")) {
                    fileVO.setFilePath(remotePathLabel.getText() + file.getName());
                } else {
                    fileVO.setFilePath(remotePathLabel.getText() + "/" + file.getName());
                }
                if (!file.isDirectory()) {
                    fileVO.setFileByteCount(file.getSize());
                    fileVO.setFileDisplaySize(FileUtils.byteCountToDisplaySize(file.getSize()));
                }
                fileVO.setFileLastModifiedTime(sdf.format(file.getTimestamp().getTime()));
                fileVO.setIsDirectory(file.isDirectory());
                fileVOList.add(fileVO);
            }
        }

        remoteFileListTableView.setItems(FXCollections.observableArrayList(fileVOList));
    }


    public FTPClient connectAndLogin() {
        return FTPService.connectAndLogin("192.168.153.130", 2121, "admin", "admin");
    }

    public void logoutAndDisconnect(FTPClient ftpClient) {
        FTPService.logoutAndDisconnect(ftpClient);
    }

    public void onLocalReloadButtonClick(ActionEvent actionEvent) {
        refreshLocalFileList();
    }

    public void onRemoteReloadButtonClick(ActionEvent actionEvent) {
        refreshRemoteFileList();
    }
}
