package cs.sbs.web.service.impl;

import cs.sbs.web.config.AiPromptProperties;
import cs.sbs.web.dto.AiAdminChapterSummaryRequest;
import cs.sbs.web.dto.AiAdminCoursePolishRequest;
import cs.sbs.web.dto.AiAdminFaqDraftRequest;
import cs.sbs.web.dto.AiAdminLessonObjectivesRequest;
import cs.sbs.web.dto.AiTextGenerationResponse;
import cs.sbs.web.entity.AiAdminGeneratedContent;
import cs.sbs.web.entity.Course;
import cs.sbs.web.repository.AiAdminGeneratedContentRepository;
import cs.sbs.web.repository.CourseRepository;
import cs.sbs.web.service.AiAdminContentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AiAdminContentServiceImpl implements AiAdminContentService {

    private final ChatClient chatClient;
    private final AiPromptProperties aiPromptProperties;
    private final CourseRepository courseRepository;
    private final AiAdminGeneratedContentRepository aiAdminGeneratedContentRepository;
    private final ObjectMapper objectMapper;

    @Override
    public AiTextGenerationResponse polishCourseIntroduction(AiAdminCoursePolishRequest request) {
        Course course = loadCourse(request.courseId());
        String userPrompt = """
                请根据下面的课程信息与管理员输入，对课程简介进行润色。

                课程标题：%s
                讲师：%s
                分类：%s
                课时数：%s
                当前简介：%s
                目标人群：%s
                风格要求：%s

                输出要求：
                1. 输出 1 段适合课程详情页展示的简介。
                2. 语言自然、专业、可信。
                3. 不要使用夸张营销词汇。
                """.formatted(
                course.getTitle(),
                course.getTeacher(),
                course.getCategory().getName(),
                course.getLessonCount(),
                request.draftIntroduction(),
                defaultIfBlank(request.targetAudience(), "线上课程学员"),
                defaultIfBlank(request.tone(), "简洁专业")
        );
        String taskType = "course-intro-polish";
        String systemPrompt = aiPromptProperties.getAdminSystemPrompt();
        String output = generateText(systemPrompt, userPrompt, request.extraBody());
        persistGenerated(taskType, course, systemPrompt, userPrompt, request, output);
        return buildResponse(taskType, course, output);
    }

    @Override
    public AiTextGenerationResponse generateChapterSummary(AiAdminChapterSummaryRequest request) {
        Course course = loadCourse(request.courseId());
        int summaryLength = request.summaryLength() == null || request.summaryLength() < 80
                ? 160
                : request.summaryLength();
        String userPrompt = """
                请为下面的线上课程章节生成摘要。

                课程标题：%s
                讲师：%s
                章节标题：%s
                章节内容：%s

                输出要求：
                1. 输出 1 段章节摘要。
                2. 摘要长度控制在 %s 字左右。
                3. 强调本章学习重点，适合课程后台运营使用。
                """.formatted(
                course.getTitle(),
                course.getTeacher(),
                request.chapterTitle(),
                request.chapterContent(),
                summaryLength
        );
        String taskType = "chapter-summary";
        String systemPrompt = aiPromptProperties.getAdminSystemPrompt();
        String output = generateText(systemPrompt, userPrompt, request.extraBody());
        persistGenerated(taskType, course, systemPrompt, userPrompt, request, output);
        return buildResponse(taskType, course, output);
    }

    @Override
    public AiTextGenerationResponse generateLessonObjectives(AiAdminLessonObjectivesRequest request) {
        Course course = loadCourse(request.courseId());
        int objectiveCount = request.objectiveCount() == null || request.objectiveCount() < 1
                ? 3
                : Math.min(request.objectiveCount(), 6);
        String userPrompt = """
                请为下面的课时生成 learning objectives。

                课程标题：%s
                章节标题：%s
                课时标题：%s
                课时内容：%s

                输出要求：
                1. 输出 %s 条学习目标。
                2. 每条目标以动词开头，强调学生学完后能够做什么。
                3. 使用中文表达，适合直接给课程运营人员参考。
                """.formatted(
                course.getTitle(),
                request.chapterTitle(),
                request.lessonTitle(),
                request.lessonContent(),
                objectiveCount
        );
        String taskType = "lesson-objectives";
        String systemPrompt = aiPromptProperties.getAdminSystemPrompt();
        String output = generateText(systemPrompt, userPrompt, request.extraBody());
        persistGenerated(taskType, course, systemPrompt, userPrompt, request, output);
        return buildResponse(taskType, course, output);
    }

    @Override
    public AiTextGenerationResponse generateFaqDraft(AiAdminFaqDraftRequest request) {
        Course course = loadCourse(request.courseId());
        int faqCount = request.faqCount() == null || request.faqCount() < 1 ? 5 : Math.min(request.faqCount(), 8);
        String userPrompt = """
                请为下面的线上课程生成 FAQ 草稿。

                课程标题：%s
                讲师：%s
                适用人群：%s
                课程素材：%s

                输出要求：
                1. 生成 %s 组常见问题与回答。
                2. 问题要贴近课程购买前咨询和学习过程中的常见疑问。
                3. 回答简洁、可信，不要编造具体价格、优惠和开课日期。
                """.formatted(
                course.getTitle(),
                course.getTeacher(),
                request.audience(),
                request.sourceMaterial(),
                faqCount
        );
        String taskType = "faq-draft";
        String systemPrompt = aiPromptProperties.getAdminSystemPrompt();
        String output = generateText(systemPrompt, userPrompt, request.extraBody());
        persistGenerated(taskType, course, systemPrompt, userPrompt, request, output);
        return buildResponse(taskType, course, output);
    }

    private String generateText(
            String systemPrompt,
            String userPrompt,
            Map<String, Object> extraBody
    ) {
        OpenAiChatOptions options = buildOptions(extraBody);
        Prompt prompt = options == null
                ? new Prompt(new SystemMessage(systemPrompt), new UserMessage(userPrompt))
                : new Prompt(
                        java.util.List.of(new SystemMessage(systemPrompt), new UserMessage(userPrompt)),
                        options
                );
        return chatClient.prompt(prompt).call().content();
    }

    private OpenAiChatOptions buildOptions(Map<String, Object> extraBody) {
        if (extraBody == null || extraBody.isEmpty()) {
            return null;
        }
        return OpenAiChatOptions.builder()
                .extraBody(extraBody)
                .build();
    }

    private void persistGenerated(
            String taskType,
            Course course,
            String systemPrompt,
            String userPrompt,
            Object request,
            String outputContent
    ) {
        AiAdminGeneratedContent content = new AiAdminGeneratedContent();
        content.setTaskType(taskType);
        content.setCourseId(course.getId());
        content.setCourseTitle(course.getTitle());
        content.setSystemPrompt(systemPrompt);
        content.setUserPrompt(userPrompt);
        content.setRequestJson(toJsonSafely(request));
        content.setOutputContent(outputContent);
        aiAdminGeneratedContentRepository.save(content);
    }

    private String toJsonSafely(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private Course loadCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("课程不存在: " + courseId));
    }

    private AiTextGenerationResponse buildResponse(String taskType, Course course, String content) {
        return new AiTextGenerationResponse(taskType, course.getId(), course.getTitle(), content);
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
