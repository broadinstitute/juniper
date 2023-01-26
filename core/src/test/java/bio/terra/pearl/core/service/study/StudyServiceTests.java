package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeTree;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StudyServiceTests extends BaseSpringBootTest {
    @Autowired
    private StudyService studyService;
    @Autowired
    private StudyFactory studyFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvFactory;

    @Autowired
    private StudyEnvironmentService studyEnvironmentService;

    @Test
    @Transactional
    public void testCreateStudy() {
        Study study = studyFactory.builder("testCreateStudy").build();
        Study savedStudy = studyService.create(study);
        Assertions.assertNotNull(savedStudy.getId());
        Assertions.assertEquals(study.getName(), savedStudy.getName());
        Assertions.assertNotNull(savedStudy.getCreatedAt());
    }

    @Test
    @Transactional
    public void testCreateStudyCascade() {
        // see if we can save a study, environment, and config
        String randPassword = RandomStringUtils.randomAlphabetic(10);
        StudyEnvironment studyEnv = studyEnvFactory.builderWithDependencies("testCreateStudyCascade")
                .studyEnvironmentConfig(StudyEnvironmentConfig.builder().password(randPassword).build())
                .build();
        Set<StudyEnvironment> studyEnvs = new HashSet<>(Arrays.asList(studyEnv));
        Study study = studyFactory.builder("testCreateStudy")
                .studyEnvironments(studyEnvs)
                .build();
        CascadeTree cascades = new CascadeTree(StudyService.AllowedCascades.STUDY_ENVIRONMENTS,
                new CascadeTree(StudyEnvironmentService.AllowedCascades.ENVIRONMENT_CONFIG));
        Study savedStudy = studyService.create(study);
        Assertions.assertNotNull(savedStudy.getId());
        Set<StudyEnvironment> savedEnvs = studyEnvironmentService.findByStudy(savedStudy.getId());
        Assertions.assertEquals(1, savedEnvs.size());
        Assertions.assertEquals(studyEnvs.stream().findFirst().get().getEnvironmentName(),
                savedEnvs.stream().findFirst().get().getEnvironmentName());
    }
}
