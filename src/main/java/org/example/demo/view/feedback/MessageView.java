package org.example.demo.view.feedback;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class MessageView implements FxmlView<MessageViewModel>, Initializable {

    @InjectViewModel
    private MessageViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void onSuccessButtonClick(ActionEvent actionEvent) {
        MessageUtils.success("成功消息");
    }

    public void onErrorButtonClick(ActionEvent actionEvent) {
        MessageUtils.error("错误消息");
    }
}
