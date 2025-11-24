package main.java.model;

import java.util.UUID;
import java.time.LocalDateTime;

public class Category {
    private UUID categoryId;
    private String name;
    private LocalDateTime createdAt;

    public Category() {

    }

    public Category(UUID categoryId, String name, LocalDateTime createdAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Category(String name) {
        this.name = name;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
