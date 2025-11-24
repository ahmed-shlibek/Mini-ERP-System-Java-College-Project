package main.java.dao;

import main.java.model.Category;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface CategoryDAO {
    Category save(Category category);

    Optional<Category> findById(UUID uuid);

    Optional<Category> findByName(String name);

    List<Category> findAll();

    void delete(UUID uuid);
}
