package org.example.demo.entity;

import lombok.Data;

@Data
public class PersonEntity {
    private Long id;
    private String name;
    private Long age;

    public PersonEntity() {
    }

    public PersonEntity(Long id, String name, Long age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
}
