package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
    @Autowired
    private PortalService portalService;

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
        return buildPersisted(portalEnvironment.getEnvironmentName(), study.getId(), testName);
    }

    public StudyEnvironment buildPersisted(EnvironmentName envName, UUID studyId, String testName) {
        StudyEnvironment studyEnv = StudyEnvironment.builder()
                .studyId(studyId)
                .environmentName(envName)
                .studyEnvironmentConfig(new StudyEnvironmentConfig()).build();
        return studyEnvironmentService.create(studyEnv);
    }

    public StudyEnvironmentBundle buildBundle(String testName, EnvironmentName envName) {
        PortalEnvironment portalEnvironment = portalEnvironmentFactory.buildPersisted(testName);
        Study study = studyFactory.buildPersisted(portalEnvironment.getPortalId(), testName);
        StudyEnvironment studyEnvironment = buildPersisted(envName, study.getId(), testName);
        Portal portal = portalService.find(portalEnvironment.getPortalId()).get();
        return StudyEnvironmentBundle.builder()
                .study(study)
                .studyEnv(studyEnvironment)
                .portal(portal)
                .portalEnv(portalEnvironment)
                .build();
    }

    @Getter @Setter
    @Builder
    public static class StudyEnvironmentBundle {
        private Study study;
        private StudyEnvironment studyEnv;
        private Portal portal;
        private PortalEnvironment portalEnv;
    }

}
