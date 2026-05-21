package cs.sbs.web.service;

import cs.sbs.web.dto.AiAdminChapterSummaryRequest;
import cs.sbs.web.dto.AiAdminCoursePolishRequest;
import cs.sbs.web.dto.AiAdminFaqDraftRequest;
import cs.sbs.web.dto.AiAdminLessonObjectivesRequest;
import cs.sbs.web.dto.AiTextGenerationResponse;

public interface AiAdminContentService {

    AiTextGenerationResponse polishCourseIntroduction(AiAdminCoursePolishRequest request);

    AiTextGenerationResponse generateChapterSummary(AiAdminChapterSummaryRequest request);

    AiTextGenerationResponse generateLessonObjectives(AiAdminLessonObjectivesRequest request);

    AiTextGenerationResponse generateFaqDraft(AiAdminFaqDraftRequest request);
}
