package org.example.demo.converter;

import javafx.util.converter.LongStringConverter;

public class FXLongStringConverter extends LongStringConverter {
    @Override
    public Long fromString(String value) {
        try {
            return super.fromString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
