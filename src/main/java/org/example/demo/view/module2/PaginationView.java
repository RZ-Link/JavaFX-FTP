package org.example.demo.view.module2;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.example.demo.component.FXPagination;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PaginationView implements FxmlView<PaginationViewModel>, Initializable {

    @InjectViewModel
    private PaginationViewModel viewModel;

    @FXML
    private VBox vbox;
    @FXML
    private FXPagination fxPagination;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxPagination.init(7L, new ArrayList<>(List.of(100L, 200L, 300L, 400L)),
                (pageSize, currentPage) -> {
                    fxPagination.update(fxPagination.getTotalItemCount().get(), pageSize, currentPage);
                });
        fxPagination.update(1000L, 100L, 1L);
    }
}
