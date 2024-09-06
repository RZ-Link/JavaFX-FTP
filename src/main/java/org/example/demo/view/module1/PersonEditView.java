package org.example.demo.view.module1;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class PersonEditView implements FxmlView<PersonEditViewModel>, Initializable {

    @FXML
    private TextField id;
    @FXML
    private TextField name;
    @FXML
    private ComboBox<String> age;


    @InjectViewModel
    private PersonEditViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        id.textProperty().bindBidirectional(viewModel.idProperty());
        name.textProperty().bindBidirectional(viewModel.nameProperty());
        age.valueProperty().bindBidirectional(viewModel.ageProperty());
    }
}
