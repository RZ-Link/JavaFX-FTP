package org.example.demo.view.module1;

import atlantafx.base.theme.Styles;
import de.saxsys.mvvmfx.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;
import org.example.demo.converter.FXLongStringConverter;
import org.example.demo.entity.TableItemEntity;

import java.net.URL;
import java.util.ResourceBundle;

public class EditableTableView implements FxmlView<EditableTableViewModel>, Initializable {
    public TableView<TableItemEntity> tableView;
    public TableColumn<TableItemEntity, String> column1;
    public TableColumn<TableItemEntity, String> column2;
    public TableColumn<TableItemEntity, Long> column3;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Styles.toggleStyleClass(tableView, Styles.BORDERED);

        column1.setCellFactory(TextField2TableCell.forTableColumn());
        column1.setCellValueFactory(new PropertyValueFactory<>("column1"));
        column1.setOnEditCommit(e -> {
            e.getRowValue().setColumn1(e.getNewValue());
            tableView.refresh();
        });

        column2.setCellFactory(TextField2TableCell.forTableColumn());
        column2.setCellValueFactory(new PropertyValueFactory<>("column2"));
        column2.setOnEditCommit(e -> {
            e.getRowValue().setColumn2(e.getNewValue());
            tableView.refresh();
        });

        column3.setCellFactory(TextField2TableCell.forTableColumn(new FXLongStringConverter()));
        column3.setCellValueFactory(new PropertyValueFactory<>("column3"));
        column3.setOnEditCommit(e -> {
            e.getRowValue().setColumn3(e.getNewValue());
            tableView.refresh();
        });
    }

    public void onAddButtonClick(ActionEvent actionEvent) {
        tableView.getItems().add(new TableItemEntity());
    }

    public void onSaveButtonClick(ActionEvent actionEvent) {
        System.out.println(tableView.getItems());
    }
}

