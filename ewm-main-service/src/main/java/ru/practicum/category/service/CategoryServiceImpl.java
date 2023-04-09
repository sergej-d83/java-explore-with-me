package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryMapper;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {

        Optional<Category> category = categoryRepository.findByName(newCategoryDto.getName());
        if (category.isPresent()) {
            throw new ConflictException("Эта категория уже существует. " + newCategoryDto.getName());
        }

        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    public CategoryDto patchCategory(Long catId, NewCategoryDto newCategoryDto) {

        Category categoryToUpdate = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена."));

        Optional<Category> category = categoryRepository.findByName(newCategoryDto.getName());
        if (category.isPresent()) {
            throw new ConflictException("Эта категория уже существует. " + newCategoryDto.getName());
        }

        categoryToUpdate.setName(newCategoryDto.getName());

        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryToUpdate));
    }

    @Override
    public void deleteCategory(Long catId) {

        categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Категория не найдена."));

        List<Event> events = eventRepository.findByCategoryId(catId);

        if (events.isEmpty()) {
            categoryRepository.deleteById(catId);
        } else {
            throw new ConflictException("Категория не может быть удалена. Одно или несколько событий связаны с ней.");
        }
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {

        Pageable pageable = PageRequest.of((from / size), size);

        return categoryRepository.findAll(pageable)
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        return CategoryMapper.toCategoryDto(categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена.")));
    }
}
