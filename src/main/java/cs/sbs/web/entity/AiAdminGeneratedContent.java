package cs.sbs.web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ai_admin_generated_content")
public class AiAdminGeneratedContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String taskType;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 200)
    private String courseTitle;

    @Lob
    @Column(nullable = false)
    private String systemPrompt;

    @Lob
    @Column(nullable = false)
    private String userPrompt;

    @Lob
    private String requestJson;

    @Lob
    @Column(nullable = false)
    private String outputContent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
