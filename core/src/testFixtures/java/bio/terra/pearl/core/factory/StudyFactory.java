package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyFactory {
    @Autowired
    private StudyService studyService;
    @Autowired
    private PortalStudyService portalStudyService;

    public Study.StudyBuilder builder(String testName) {
        return Study.builder()
                .name(testName + RandomStringUtils.randomAlphabetic(6))
                .shortcode(RandomStringUtils.randomAlphabetic(7));
    }

    public Study.StudyBuilder builderWithDependencies(String testName) {
        return builder(testName);
    }

    public Study buildPersisted(String testName) {
        return studyService.create(builderWithDependencies(testName).build());
    }

    public Study buildPersisted(UUID portalId, String testName) {
        Study study = studyService.create(builderWithDependencies(testName).build());
        portalStudyService.create(portalId, study.getId());
        return study;
    }
}
