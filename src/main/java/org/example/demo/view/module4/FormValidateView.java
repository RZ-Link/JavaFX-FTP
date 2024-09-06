package org.example.demo.view.module4;

import de.saxsys.mvvmfx.FxmlView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;


public class FormValidateView implements FxmlView<FormValidateViewModel>, Initializable {

    public TipVBox activityName;
    public TipHBox activityZone;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void onSaveButtonClick() {
        activityName.showTip("请输入活动名称");
        activityZone.showTip("请输入活动区域");
    }

    @FXML
    private void onResetButtonClick() {
        activityName.hideTip();
        activityZone.hideTip();
    }
}
