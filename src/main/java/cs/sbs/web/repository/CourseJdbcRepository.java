package cs.sbs.web.repository;

import cs.sbs.web.dto.CourseSqlViewResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CourseJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public CourseJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CourseSqlViewResponse> findHotPublishedCourses(int limit) {
        String sql = """
                select c.id,
                       c.title,
                       c.teacher,
                       cg.name as category_name,
                       c.lesson_count,
                       c.price
                from course c
                join category cg on c.category_id = cg.id
                where c.published = true
                order by c.lesson_count desc, c.price desc, c.id desc
                limit ?
                """;
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new CourseSqlViewResponse(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("teacher"),
                        rs.getString("category_name"),
                        rs.getInt("lesson_count"),
                        rs.getBigDecimal("price")
                ),
                limit
        );
    }

    public int countCoursesThatWillBecomeNegative(Long categoryId, BigDecimal delta) {
        String sql = """
                select count(*)
                from course
                where category_id = ?
                  and price + ? < 0
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, categoryId, delta);
        return count == null ? 0 : count;
    }

    public int increaseCategoryCoursePrice(Long categoryId, BigDecimal delta) {
        String sql = """
                update course
                set price = price + ?,
                    updated_at = current_timestamp()
                where category_id = ?
                """;
        return jdbcTemplate.update(sql, delta, categoryId);
    }
}
