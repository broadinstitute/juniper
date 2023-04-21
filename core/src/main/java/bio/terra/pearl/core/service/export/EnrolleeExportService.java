package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExportService {
    public static final String ENROLLEE_PREFIX = "enrollee";
    public Map<String, String> generateExportMap(Enrollee enrollee, List<Answer> answers, Profile profile,
                                                 List<SurveyQuestionDefinition> definitions) throws Exception {
        Map<String, String> valueMap = mapEnrollee(enrollee);
        valueMap.putAll(ProfileFormatter.mapProfile(profile));
        return valueMap;
    }

    public static Map<String, String> mapEnrollee(Enrollee enrollee) {
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put(ENROLLEE_PREFIX + ExportFormatUtils.COLUMN_NAME_DELIMITER + "shortcode",
                enrollee.getShortcode());
        valueMap.put(ENROLLEE_PREFIX + ExportFormatUtils.COLUMN_NAME_DELIMITER + "consented",
                ExportFormatUtils.formatForExport(enrollee.isConsented()));
        return valueMap;
    }
}
