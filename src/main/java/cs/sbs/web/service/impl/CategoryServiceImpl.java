package cs.sbs.web.service.impl;

import cs.sbs.web.dto.CategoryCreateRequest;
import cs.sbs.web.dto.CategoryResponse;
import cs.sbs.web.entity.Category;
import cs.sbs.web.repository.CategoryRepository;
import cs.sbs.web.repository.CourseRepository;
import cs.sbs.web.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    @Override
    public CategoryResponse create(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("分类名称已存在: " + request.name());
        }
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        if (request.parentId() != null) {
            Category parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("父分类不存在: " + request.parentId()));
            category.setParent(parent);
        }
        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAllByOrderBySortOrderAscIdAsc()
                .stream()
                .map(this::toFlatResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("分类不存在: " + categoryId));
        return toFlatResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findByParentIsNullOrderBySortOrderAscIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("分类不存在: " + categoryId));
        if (categoryRepository.existsByParentId(categoryId)) {
            throw new IllegalArgumentException("该分类下仍有子分类，不能直接删除");
        }
        if (courseRepository.existsByCategoryId(categoryId)) {
            throw new IllegalArgumentException("该分类下仍有关联课程，不能直接删除");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category category) {
        List<CategoryResponse> children = category.getChildren()
                .stream()
                .map(this::toResponse)
                .toList();
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                category.getParent() == null ? null : category.getParent().getId(),
                children
        );
    }

    private CategoryResponse toFlatResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                category.getParent() == null ? null : category.getParent().getId(),
                List.of()
        );
    }
}
