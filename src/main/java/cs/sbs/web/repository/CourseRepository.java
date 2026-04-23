package cs.sbs.web.repository;

import cs.sbs.web.entity.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    List<Course> findByTeacherContainingIgnoreCaseOrderByIdDesc(String teacherKeyword);

    boolean existsByCategoryId(Long categoryId);
}
