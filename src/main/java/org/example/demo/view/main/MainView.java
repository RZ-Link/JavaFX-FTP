package org.example.demo.view.main;

import atlantafx.base.theme.Styles;
import cn.hutool.core.util.StrUtil;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.example.demo.component.SideMenu;
import org.example.demo.view.feedback.DialogSampleView;
import org.example.demo.view.feedback.MessageView;
import org.example.demo.view.module1.CenterTableView;
import org.example.demo.view.module1.EditableTableView;
import org.example.demo.view.module1.GroupingHeadTableView;
import org.example.demo.view.module1.TableView;
import org.example.demo.view.module2.PaginationView;
import org.example.demo.view.module3.FileIconView;
import org.example.demo.view.module3.ReadFontView;
import org.example.demo.view.module3.ReadImageView;
import org.example.demo.view.module4.FormValidateView;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainView implements FxmlView<MainViewModel>, Initializable {

    @FXML
    private BorderPane borderPane;

    private SideMenu sideMenu;
    private TabPane tabPane;

    @InjectViewModel
    private MainViewModel viewModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 左侧菜单
        sideMenu = new SideMenu();
        sideMenu.init(this);
        // 右侧标签
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabPane.getStyleClass().add(Styles.TABS_CLASSIC);

        borderPane.setLeft(sideMenu);
        borderPane.setCenter(tabPane);
    }

    public void updateTabPane(String tabTitle) {
        Tab tab;
        // 查询TabPane是否已经存在需要展示的Tab。如果已经存在，直接切换。
        Optional<Tab> tabOptional = tabPane.getTabs().stream()
                .filter(t -> StrUtil.equals(t.getText(), tabTitle))
                .findFirst();
        if (tabOptional.isPresent()) {
            tab = tabOptional.get();
        } else {
            tab = new Tab(tabTitle);
            switch (tabTitle) {
                case "基础表格":
                    tab.setContent(FluentViewLoader.fxmlView(TableView.class).load().getView());
                    break;
                case "可编辑表格":
                    tab.setContent(FluentViewLoader.fxmlView(EditableTableView.class).load().getView());
                    break;
                case "表头分组表格":
                    tab.setContent(FluentViewLoader.fxmlView(GroupingHeadTableView.class).load().getView());
                    break;
                case "居中表格":
                    tab.setContent(FluentViewLoader.fxmlView(CenterTableView.class).load().getView());
                    break;
                case "分页组件":
                    tab.setContent(FluentViewLoader.fxmlView(PaginationView.class).load().getView());
                    break;
                case "图片读取":
                    tab.setContent(FluentViewLoader.fxmlView(ReadImageView.class).load().getView());
                    break;
                case "字体读取":
                    tab.setContent(FluentViewLoader.fxmlView(ReadFontView.class).load().getView());
                    break;
                case "FileIcon读取":
                    tab.setContent(FluentViewLoader.fxmlView(FileIconView.class).load().getView());
                    break;
                case "消息提示":
                    tab.setContent(FluentViewLoader.fxmlView(MessageView.class).load().getView());
                    break;
                case "消息弹出框":
                    tab.setContent(FluentViewLoader.fxmlView(DialogSampleView.class).load().getView());
                    break;
                case "表单校验":
                    tab.setContent(FluentViewLoader.fxmlView(FormValidateView.class).load().getView());
                    break;
                default:
                    return;
            }
            tabPane.getTabs().add(tab);
        }
        tabPane.getSelectionModel().select(tab);
    }
}
