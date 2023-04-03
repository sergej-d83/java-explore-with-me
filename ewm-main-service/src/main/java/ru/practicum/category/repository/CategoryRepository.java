package ru.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
