package cs.sbs.web.service;

import cs.sbs.web.dto.CategoryCreateRequest;
import cs.sbs.web.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {

    CategoryResponse create(CategoryCreateRequest request);

    List<CategoryResponse> listAll();

    CategoryResponse getById(Long categoryId);

    List<CategoryResponse> getCategoryTree();

    List<CategoryResponse> qbeSearch(String name, String description);

    void deleteById(Long categoryId);
}
