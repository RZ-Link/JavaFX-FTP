package org.example.demo.component;

import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.example.demo.view.main.MainView;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;

// 左侧菜单
public class SideMenu extends StackPane {

    private MainView mainView;

    public SideMenu() {

        HashMap<TreeItem<String>, Ikon> treeItemIconMap = new HashMap<>();

        // 创建根节点
        TreeItem<String> rootItem = new TreeItem<>();

        // 创建子节点
        TreeItem<String> child1 = new TreeItem<>("表格");
        treeItemIconMap.put(child1, Feather.COLUMNS);
        TreeItem<String> subChild11 = new TreeItem<>("基础表格");
        TreeItem<String> subChild12 = new TreeItem<>("可编辑表格");
        TreeItem<String> subChild13 = new TreeItem<>("表头分组表格");
        TreeItem<String> subChild14 = new TreeItem<>("居中表格");
        child1.getChildren().addAll(subChild11, subChild12, subChild13, subChild14);
        child1.setExpanded(true);

        TreeItem<String> child2 = new TreeItem<>("分页组件");
        treeItemIconMap.put(child2, Feather.FAST_FORWARD);

        TreeItem<String> child3 = new TreeItem<>("资源读取");
        treeItemIconMap.put(child3, Feather.AT_SIGN);
        TreeItem<String> subChild31 = new TreeItem<>("图片读取");
        TreeItem<String> subChild32 = new TreeItem<>("字体读取");
        TreeItem<String> subChild33 = new TreeItem<>("FileIcon读取");
        child3.getChildren().addAll(subChild31, subChild32, subChild33);
        child3.setExpanded(true);

        TreeItem<String> child4 = new TreeItem<>("反馈组件");
        treeItemIconMap.put(child4, Feather.MESSAGE_CIRCLE);
        TreeItem<String> subChild41 = new TreeItem<>("消息提示");
        TreeItem<String> subChild42 = new TreeItem<>("消息弹出框");
        child4.getChildren().addAll(subChild41, subChild42);
        child4.setExpanded(true);

        TreeItem<String> child5 = new TreeItem<>("表单");
        treeItemIconMap.put(child5, Feather.FILE_TEXT);
        TreeItem<String> subChild51 = new TreeItem<>("表单校验");
        child5.getChildren().addAll(subChild51);
        child5.setExpanded(true);

        // 根节点添加子节点
        rootItem.getChildren().addAll(child1, child2, child3, child4, child5);
        rootItem.setExpanded(true);

        // 创建TreeView，设置根节点
        TreeView<String> treeView = new TreeView<>(rootItem);
        // 隐藏根节点
        treeView.setShowRoot(false);
        // 设置TreeCell
        treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> stringTreeView) {
                TreeCell<String> treeCell = new TreeCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        // 隐藏折叠图标
                        setDisclosureNode(null);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(null);

                            Label label = new Label(item);
                            if (treeItemIconMap.containsKey(this.getTreeItem())) {
                                label.setGraphic(FontIcon.of(treeItemIconMap.get(this.getTreeItem()))); // 设置图标
                            }
                            label.setGraphicTextGap(10);
                            setGraphic(label);
                        }
                    }
                };
                // 如果当前模块存在子模块，点击展开/折叠菜单
                treeCell.setOnMouseClicked(event -> {
                    if (!treeCell.isEmpty()) {
                        treeCell.getTreeItem().setExpanded(!treeCell.getTreeItem().isExpanded());
                    }
                });
                return treeCell;
            }
        });

        // 监听用户选择模块变化，推送事件（更新窗口右侧页面）
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isLeaf()) {
                if (mainView != null) {
                    mainView.updateTabPane(newValue.getValue());
                }
            }
        });

        // StackPane添加TreeView
        getChildren().addAll(treeView);
    }

    public void init(MainView mainView) {
        this.mainView = mainView;
    }

}
