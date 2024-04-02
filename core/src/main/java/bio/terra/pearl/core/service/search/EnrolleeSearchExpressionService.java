package bio.terra.pearl.core.service.search;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.search.terms.ProfileTerm;
import bio.terra.pearl.core.service.search.terms.SearchValue;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnrolleeSearchExpressionService {

    private final SurveyService surveyService;
    private final PortalService portalService;

    public EnrolleeSearchExpressionService(SurveyService surveyService, PortalService portalService) {
        this.surveyService = surveyService;
        this.portalService = portalService;
    }

    public Map<String, SearchValue.SearchValueType> getAllFields(String portalShortcode, String studyShortcode, EnvironmentName envName) {
        Portal portal = portalService.findOneByShortcode(portalShortcode).orElseThrow();

        Map<String, SearchValue.SearchValueType> fields = new HashMap<>();
        // profile fields
        ProfileTerm.FIELDS.forEach((term, type) -> fields.put("profile."+term, type));
        // age
        fields.put("age", SearchValue.SearchValueType.INTEGER);
        // answers
        List<Survey> surveys = surveyService.findByPortalId(portal.getId());
        for (Survey survey : surveys) {
            surveyService
                    .getSurveyQuestionDefinitions(survey)
                    .forEach(def -> {
                        fields.put("answer." + def.getSurveyStableId() + "." + def.getQuestionStableId(), SearchValue.SearchValueType.STRING);
                    });
        }

        return fields;
    }
}
