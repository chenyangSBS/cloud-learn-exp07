package cs.sbs.web.config;

import cs.sbs.web.entity.Category;
import cs.sbs.web.entity.Course;
import cs.sbs.web.repository.CategoryRepository;
import cs.sbs.web.repository.CourseRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public DataInitializer(CategoryRepository categoryRepository, CourseRepository courseRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0 || courseRepository.count() > 0) {
            return;
        }

        Category programming = saveCategory("编程开发", "软件开发与工程实践课程", 1, null);
        Category frontend = saveCategory("前端开发", "Web 前端方向", 1, programming);
        Category backend = saveCategory("后端开发", "Java 与服务端方向", 2, programming);
        Category database = saveCategory("数据库", "数据库设计与调优", 3, programming);
        Category design = saveCategory("产品设计", "交互与原型设计课程", 2, null);

        saveCourse("Spring Boot + JPA 实战", "张老师", "讲解实体映射、Repository 与分页查询", new BigDecimal("199.00"), 18, backend, true);
        saveCourse("Vue 3 企业级开发", "李老师", "覆盖组件通信、路由和 Pinia", new BigDecimal("149.00"), 20, frontend, true);
        saveCourse("SQL 性能优化入门", "王老师", "聚焦索引、执行计划与常见调优方式", new BigDecimal("99.00"), 12, database, false);
        saveCourse("Figma 原型设计基础", "赵老师", "适合教学演示的原型设计课程", new BigDecimal("89.00"), 10, design, true);
        saveCourse("Java Web 项目实训", "陈老师", "综合案例串联认证、JPA 与接口开发", new BigDecimal("219.00"), 24, backend, true);
    }

    private Category saveCategory(String name, String description, int sortOrder, Category parent) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSortOrder(sortOrder);
        category.setParent(parent);
        return categoryRepository.save(category);
    }

    private void saveCourse(String title, String teacher, String summary, BigDecimal price,
                            int lessonCount, Category category, boolean published) {
        Course course = new Course();
        course.setTitle(title);
        course.setTeacher(teacher);
        course.setSummary(summary);
        course.setPrice(price);
        course.setLessonCount(lessonCount);
        course.setCategory(category);
        course.setPublished(published);
        courseRepository.save(course);
    }
}
