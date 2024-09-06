package org.example.demo.component;

import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import lombok.Data;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * NewFXPagination
 *
 * @author ruiqi
 * @date 2024/8/28 11:18
 */
@Data
public class NewFXPagination extends HBox {
    // 总条目数
    private SimpleLongProperty totalItemCount;
    // 每页显示条目个数
    private SimpleLongProperty pageSize;
    // 当前页数
    private SimpleLongProperty currentPage;
    // 最大显示页数
    private int maxShowPageNum = 1;
    // currentPage改变时触发
    private BiConsumer<Long, Long> currentChange;

    private Label totalItemCountLabel;
    private ComboBox<String> pageSizeComboBox;
    private Button prevButton;
    // private Button currentPageButton;
    private Button nextButton;
    private Label gotoLabel;
    private TextField gotoTextField;
    private Label totalPageCountLabel;


    public NewFXPagination() {

        this.totalItemCount = new SimpleLongProperty(0L);
        this.pageSize = new SimpleLongProperty(10L);
        this.currentPage = new SimpleLongProperty(1L);
        this.currentChange = (pageSize, currentPage) -> {
            this.update(this.totalItemCount.get(), pageSize, currentPage);
        };

        this.totalItemCountLabel = new Label(StrUtil.format("Total item count {}", this.totalItemCount.get()));

        this.pageSizeComboBox = new ComboBox<>();
        for (Long item : List.of(10L, 20L, 30L, 40L, 50L, 100L)) {
            this.pageSizeComboBox.getItems().add(StrUtil.format("{}/page", item));
        }
        this.pageSizeComboBox.setValue(StrUtil.format("{}/page", pageSize.get()));
        this.pageSizeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                long newPageSize = Long.parseLong(newValue.replace("/page", ""));
                this.pageSize.set(newPageSize);

                long newPageCount = this.getPageCount(this.totalItemCount.get(), newPageSize);
                if (this.currentPage.get() > newPageCount) {
                    this.currentPage.set(newPageCount);
                }

                this.update(this.totalItemCount.get(), this.pageSize.get(), this.currentPage.get());
            }
        });

        this.prevButton = new Button();
        this.prevButton.setGraphic(FontIcon.of(Feather.CHEVRON_LEFT));
        this.prevButton.setOnAction(event -> {
            this.currentChange.accept(this.pageSize.get(), this.currentPage.get() - 1);
        });
        this.prevButton.setDisable(this.currentPage.get() <= 1);

        int[] rainbow = PageUtil.rainbow((int)currentPage.get(), this.getPageCount().intValue(), maxShowPageNum);
        Button[] showPageButtons = new Button[rainbow.length];
        for(int i=0;i<rainbow.length;i++){
            showPageButtons[i] = new Button(String.valueOf(rainbow[i]));
            if(rainbow[i]==currentPage.get()){
                showPageButtons[i].setDisable(true);
            }else{
                long gotoPage = rainbow[i];
                showPageButtons[i].setOnAction(e->{
                    this.currentChange.accept(this.pageSize.get(), gotoPage);
                });
            }
        }
        // this.currentPageButton = new Button(String.valueOf(this.currentPage.get()));
        // this.currentPageButton.setDisable(true);

        this.nextButton = new Button();
        this.nextButton.setGraphic(FontIcon.of(Feather.CHEVRON_RIGHT));
        this.nextButton.setOnAction(event -> {
            this.currentChange.accept(this.pageSize.get(), this.currentPage.get() + 1);
        });
        long pageCount = this.getPageCount();
        this.nextButton.setDisable(this.currentPage.get() >= pageCount);

        this.gotoLabel = new Label("Go to");

        this.gotoTextField = new TextField();
        this.gotoTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    String text = this.gotoTextField.getText();
                    long gotoPage = Long.parseLong(text);
                    if (gotoPage >= 1 && gotoPage <= this.getPageCount()) {
                        this.currentChange.accept(this.pageSize.get(), gotoPage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.totalPageCountLabel = new Label(StrUtil.format("Total page count {}", pageCount));

        this.getChildren().addAll(this.totalItemCountLabel, this.pageSizeComboBox, this.prevButton);
        for(Button button:showPageButtons){
            this.getChildren().add(button);
        }
        this.getChildren().addAll(this.nextButton, this.gotoLabel, this.gotoTextField, this.totalPageCountLabel);

        this.setAlignment(Pos.CENTER);
        this.setSpacing(5);
    }

    /**
     * 更新pageSizes & currentChange
     *
     * @param pageSizes     每页显示个数选择器的选项设置
     * @param currentChange currentPage 改变时触发
     */
    public void update(ArrayList<Long> pageSizes, BiConsumer<Long, Long> currentChange) {
        if (pageSizes != null && !pageSizes.isEmpty()) {
            this.pageSizeComboBox.getItems().clear();
            for (Long item : pageSizes) {
                this.pageSizeComboBox.getItems().add(StrUtil.format("{}/page", item));
            }
            this.pageSizeComboBox.setValue(StrUtil.format("{}/page", pageSizes.get(0)));
        }
        if (currentChange != null) {
            this.currentChange = currentChange;
        }
    }

    /**
     * 更新数据 & UI
     *
     * @param totalItemCount 总条目数
     * @param pageSize       每页显示条目个数
     * @param currentPage    当前页数
     */
    public void update(Long totalItemCount, Long pageSize, Long currentPage) {
        // 更新数据
        this.totalItemCount.set(totalItemCount);
        this.pageSize.set(pageSize);
        this.currentPage.set(currentPage);
        // 更新UI
        this.totalItemCountLabel.setText(StrUtil.format("Total item count {}", this.totalItemCount.get()));
        this.prevButton.setDisable(this.currentPage.get() <= 1);
        // this.currentPageButton.setText(String.valueOf(this.currentPage.get()));
        int[] rainbow = PageUtil.rainbow(currentPage.intValue(), this.getPageCount().intValue(), maxShowPageNum);
        Button[] showPageButtons = new Button[rainbow.length];
        for(int i=0;i<rainbow.length;i++){
            showPageButtons[i] = new Button(String.valueOf(rainbow[i]));
            if(rainbow[i]==currentPage.intValue()){
                showPageButtons[i].setDisable(true);
            }else{
                long gotoPage = rainbow[i];
                showPageButtons[i].setOnAction(e->{
                    this.currentChange.accept(this.pageSize.get(), gotoPage);
                });
            }
        }
        long pageCount = this.getPageCount();
        this.nextButton.setDisable(this.currentPage.get() >= pageCount);
        this.totalPageCountLabel.setText(StrUtil.format("Total page count {}", pageCount));
        this.getChildren().clear();
        this.getChildren().addAll(this.totalItemCountLabel, this.pageSizeComboBox, this.prevButton);
        for(Button button:showPageButtons){
            this.getChildren().add(button);
        }
        this.getChildren().addAll(this.nextButton, this.gotoLabel, this.gotoTextField, this.totalPageCountLabel);
    }


    /**
     * @param totalItemCount 总条目数
     * @param pageSize       每页显示条目个数
     * @return 总页数
     */
    private Long getPageCount(Long totalItemCount, Long pageSize) {
        long pageCount = totalItemCount / pageSize;
        if (totalItemCount % pageSize > 0) {
            pageCount++;
        }
        return pageCount;
    }

    private Long getPageCount() {
        return this.getPageCount(this.totalItemCount.get(), this.pageSize.get());
    }

}
