package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentFactory {
    @Autowired
    private EnvironmentFactory environmentFactory;
    @Autowired
    private StudyFactory studyFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private StudyEnvironmentService studyEnvironmentService;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private PortalParticipantUserService portalParticipantUserService;

    public StudyEnvironment.StudyEnvironmentBuilder builder(String testName) {
        EnvironmentName envName = EnvironmentName.values()[RandomUtils.nextInt(0, 3)];
        return StudyEnvironment.builder()
                .environmentName(envName);
    }

    public StudyEnvironment.StudyEnvironmentBuilder builderWithDependencies(String testName) {
        Study study = studyFactory.buildPersisted(testName);
        return builder(testName)
                .studyId(study.getId())
                .studyEnvironmentConfig(new StudyEnvironmentConfig())
                .environmentName(environmentFactory.buildPersisted(testName).getName());
    }

    public StudyEnvironment buildPersisted(String testName) {
        return studyEnvironmentService.create(builderWithDependencies(testName).build());
    }

    public StudyEnvironment buildPersisted(PortalEnvironment portalEnvironment, String testName) {
        Study study = studyFactory.buildPersisted(portalEnvironment.getPortalId(), testName);
        StudyEnvironment studyEnv = StudyEnvironment.builder()
                .studyId(study.getId())
                .environmentName(portalEnvironment.getEnvironmentName())
                .studyEnvironmentConfig(new StudyEnvironmentConfig()).build();
        return studyEnvironmentService.create(studyEnv);
    }
}
