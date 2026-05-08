package cs.sbs.web.controller;

import cs.sbs.web.dto.ApiResponse;
import cs.sbs.web.dto.CategoryCreateRequest;
import cs.sbs.web.dto.CategoryResponse;
import cs.sbs.web.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ApiResponse.ok("分类创建成功", categoryService.create(request));
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok("分类列表查询成功", categoryService.listAll());
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> detail(@PathVariable Long categoryId) {
        return ApiResponse.ok("分类详情查询成功", categoryService.getById(categoryId));
    }

    @GetMapping("/tree")
    public ApiResponse<List<CategoryResponse>> tree() {
        return ApiResponse.ok("分类树查询成功", categoryService.getCategoryTree());
    }

    @GetMapping("/qbe")
    public ApiResponse<List<CategoryResponse>> qbeSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description) {
        return ApiResponse.ok("分类 QBE 查询成功", categoryService.qbeSearch(name, description));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> delete(@PathVariable Long categoryId) {
        categoryService.deleteById(categoryId);
        return ApiResponse.ok("分类删除成功", null);
    }
}
