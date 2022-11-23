package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.Study;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

@Component
public class StudyFactory {
    private Study.StudyBuilder builder(String testName) {
        return Study.builder()
                .name(testName + RandomStringUtils.randomAlphabetic(6))
                .shortcode(RandomStringUtils.randomAlphabetic(7));
    }
}
