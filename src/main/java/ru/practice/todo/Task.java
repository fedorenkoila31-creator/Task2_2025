package ru.practice.todo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {

    private final int id;
    private String title;

    @JsonCreator
    public Task(@JsonProperty("id") int id, @JsonProperty("title") String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
