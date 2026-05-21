package cs.sbs.web.controller;

import cs.sbs.web.dto.AiAdminChapterSummaryRequest;
import cs.sbs.web.dto.AiAdminCoursePolishRequest;
import cs.sbs.web.dto.AiAdminFaqDraftRequest;
import cs.sbs.web.dto.AiAdminLessonObjectivesRequest;
import cs.sbs.web.dto.AiTextGenerationResponse;
import cs.sbs.web.dto.ApiResponse;
import cs.sbs.web.service.AiAdminContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/admin")
@RequiredArgsConstructor
public class AiAdminController {

    private final AiAdminContentService aiAdminContentService;

    @PostMapping("/course-intro/polish")
    public ApiResponse<AiTextGenerationResponse> polishCourseIntroduction(
            @Valid @RequestBody AiAdminCoursePolishRequest request) {
        return ApiResponse.ok("课程简介润色成功", aiAdminContentService.polishCourseIntroduction(request));
    }

    @PostMapping("/chapter-summary")
    public ApiResponse<AiTextGenerationResponse> generateChapterSummary(
            @Valid @RequestBody AiAdminChapterSummaryRequest request) {
        return ApiResponse.ok("章节摘要生成成功", aiAdminContentService.generateChapterSummary(request));
    }

    @PostMapping("/lesson-objectives")
    public ApiResponse<AiTextGenerationResponse> generateLessonObjectives(
            @Valid @RequestBody AiAdminLessonObjectivesRequest request) {
        return ApiResponse.ok("课时学习目标生成成功", aiAdminContentService.generateLessonObjectives(request));
    }

    @PostMapping("/faq-draft")
    public ApiResponse<AiTextGenerationResponse> generateFaqDraft(
            @Valid @RequestBody AiAdminFaqDraftRequest request) {
        return ApiResponse.ok("FAQ 草稿生成成功", aiAdminContentService.generateFaqDraft(request));
    }
}
