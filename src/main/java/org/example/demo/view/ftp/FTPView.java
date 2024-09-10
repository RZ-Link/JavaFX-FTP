package org.example.demo.view.ftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import de.saxsys.mvvmfx.FxmlView;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;
import org.example.demo.DemoApplication;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class FTPView implements FxmlView<FTPViewModel>, Initializable {
    public VBox localBox;
    public HBox localFileListBox;
    public TableView<FileVO> localFileListTableView;
    public TableColumn<FileVO, String> localFileNameColumn;
    public TableColumn<FileVO, String> localFileSizeColumn;
    public TableColumn<FileVO, String> localFileLastModifiedTimeColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeLocal();
    }

    public void initializeLocal() {

        localBox.prefWidthProperty().bind(DemoApplication.stage.widthProperty().multiply(0.5));
        localFileListBox.prefHeightProperty().bind(DemoApplication.stage.heightProperty().multiply(0.5));

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
}
