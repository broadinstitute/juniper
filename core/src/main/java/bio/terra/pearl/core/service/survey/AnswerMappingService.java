package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.AnswerMappingTargetType;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.portal.PortalService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AnswerMappingService extends ImmutableEntityService<AnswerMapping, AnswerMappingDao> {
    private final SurveyService surveyService;
    private final PortalService portalService;

    public AnswerMappingService(AnswerMappingDao dao, SurveyService surveyService, PortalService portalService) {
        super(dao);
        this.surveyService = surveyService;
        this.portalService = portalService;
    }

    public Optional<AnswerMapping> findByTargetField(String portalShortcode, String surveyStableId, int surveyVersion, AnswerMappingTargetType type, String targetField) {

        Survey survey = surveyService.findByStableIdAndPortalShortcode(surveyStableId, surveyVersion, portalShortcode)
                .orElseThrow(() -> new IllegalArgumentException("Survey not found"));

        return findByTargetField(survey.getId(), type, targetField);
    }

    public Optional<AnswerMapping> findByTargetField(UUID surveyId, AnswerMappingTargetType type, String targetField) {
        return dao.findByTargetField(surveyId, type, targetField);
    }
}
