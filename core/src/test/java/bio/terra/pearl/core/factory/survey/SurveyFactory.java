package bio.terra.pearl.core.factory.survey;

import bio.terra.pearl.core.model.survey.Survey;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class SurveyFactory {
    public Survey.SurveyBuilder builder(String testName) {
        String randString = RandomStringUtils.randomAlphabetic(3);
        return Survey.builder().version(1)
                .stableId(testName + "_" + randString)
                .name("Name " + randString + " survey");
    }
}
