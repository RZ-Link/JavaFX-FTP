package org.example.demo.view.login;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginView implements FxmlView<LoginViewModel>, Initializable {

    @InjectViewModel
    private LoginViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    protected void onLoginButtonClick() {
        viewModel.getWindowView().showMainView();
    }
}
