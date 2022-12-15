package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeFactory {
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    public Enrollee.EnrolleeBuilder builder(String testName) {
        return Enrollee.builder()
                .withdrawn(false);
    }

    public Enrollee.EnrolleeBuilder builderWithDependencies(String testName) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        ParticipantUser participantUser = participantUserFactory.buildPersisted(
                participantUserFactory.builder(testName)
                        .environmentName(studyEnv.getEnvironmentName()),
                testName
        );
        return builder(testName)
                .participantUserId(participantUser.getId())
                .studyEnvironmentId(studyEnv.getId());
    }

    public Enrollee buildPersisted(String testName) {
        return enrolleeService.create(builderWithDependencies(testName).build());
    }
}
