package org.example.demo.view.module1;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PersonEditViewModel implements ViewModel {

    private StringProperty id = new SimpleStringProperty("");
    private StringProperty name = new SimpleStringProperty("");
    private StringProperty age = new SimpleStringProperty("");

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getAge() {
        return age.get();
    }

    public StringProperty ageProperty() {
        return age;
    }

    public void setAge(String age) {
        this.age.set(age);
    }
}
