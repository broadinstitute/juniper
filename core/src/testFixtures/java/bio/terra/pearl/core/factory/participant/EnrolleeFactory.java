package bio.terra.pearl.core.factory.participant;

import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EnrolleeFactory {
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private StudyService studyService;
    @Autowired
    private PortalService portalService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private PortalParticipantUserService portalParticipantUserService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private RegistrationService registrationService;

    public Enrollee.EnrolleeBuilder builder(String testName) {
        return Enrollee.builder();
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

    public Enrollee.EnrolleeBuilder builderWithDependencies(String testName, StudyEnvironment studyEnv) {
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
        return buildPersisted(builderWithDependencies(testName));
    }

    public Enrollee buildPersisted(Enrollee.EnrolleeBuilder builder) {
        return enrolleeService.create(builder.build());
    }

    /** saves the given profile and creates an enrollee with that profile attached */
    public Enrollee buildPersisted(String testName, StudyEnvironment studyEnv, Profile profile) {
        Profile savedProfile = profileService.create(profile, DataAuditInfo.builder().build());
        Enrollee.EnrolleeBuilder builder = builderWithDependencies(testName, studyEnv)
            .profileId(savedProfile.getId());
        return buildPersisted(builder);
    }

    public Enrollee buildPersisted(String testName, UUID studyEnvironmentId, UUID participantUserId, UUID profileId) {
        Enrollee enrollee = Enrollee.builder()
                .studyEnvironmentId(studyEnvironmentId)
                .participantUserId(participantUserId)
                .profileId(profileId)
                .build();
        return enrolleeService.create(enrollee);
    }

    public Enrollee buildPersisted(String testName, StudyEnvironment studyEnv) {
        return enrolleeService.create(builderWithDependencies(testName, studyEnv).build());
    }

    public EnrolleeBundle buildWithPortalUser(String testName) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);
        return buildWithPortalUser(testName, portalEnv, studyEnv);
    }

    public EnrolleeBundle buildWithPortalUser(String testName, PortalEnvironment portalEnv, StudyEnvironment studyEnv) {
        ParticipantUser user = participantUserFactory.buildPersisted(studyEnv.getEnvironmentName(), testName);
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .participantUserId(user.getId())
                .portalEnvironmentId(portalEnv.getId()).build();
        ppUser = portalParticipantUserService.create(ppUser);
        Enrollee enrollee = buildPersisted(testName, studyEnv.getId(), user.getId(), ppUser.getProfileId());
        enrollee.setProfile(ppUser.getProfile());
        return new EnrolleeBundle(enrollee, user, ppUser, portalEnv.getPortalId());
    }

    public EnrolleeBundle buildWithPortalUser(String testName, PortalEnvironment portalEnv, StudyEnvironment studyEnv, Profile profile) {
        ParticipantUser user = participantUserFactory.buildPersisted(studyEnv.getEnvironmentName(), testName);
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .profile(profile)
                .participantUserId(user.getId())
                .portalEnvironmentId(portalEnv.getId()).build();
        ppUser = portalParticipantUserService.create(ppUser);
        Enrollee enrollee = buildPersisted(testName, studyEnv.getId(), user.getId(), ppUser.getProfileId());
        enrollee.setProfile(ppUser.getProfile());
        return new EnrolleeBundle(enrollee, user, ppUser, portalEnv.getPortalId());
    }

    public EnrolleeAndProxy buildProxyAndGovernedEnrollee(String testName, String proxyEmail){
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, testName, proxyEmail);
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        HubResponse<Enrollee> hubResponse = enrollmentService.enrollAsProxy(studyEnv.getEnvironmentName(), studyShortcode, userBundle.user(), userBundle.ppUser(),
                null);
        return new EnrolleeAndProxy(hubResponse.getResponse(), hubResponse.getEnrollee(), userBundle.ppUser(), portalEnv);
    }

    public EnrolleeAndProxy buildProxyAndGovernedEnrollee(String testName, PortalEnvironment portalEnv, StudyEnvironment studyEnv) {
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv, testName, testName + "_" + RandomStringUtils.randomAlphabetic(10) + "@example.com");
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        HubResponse<Enrollee> hubResponse = enrollmentService.enrollAsProxy(studyEnv.getEnvironmentName(), studyShortcode, userBundle.user(), userBundle.ppUser(),
                null);
        return new EnrolleeAndProxy(hubResponse.getResponse(), hubResponse.getEnrollee(), userBundle.ppUser(), portalEnv);
    }

    /** Enrolls a participant in a study and returns the created enrollee -- this also handles registration */
    public EnrolleeBundle enroll(String email, String portalShortcode, String studyShortcode, EnvironmentName environmentName) {
        RegistrationService.RegistrationResult result = registrationService.register(portalShortcode, environmentName, email, null, null);
        Portal portal = portalService.findOneByShortcode(portalShortcode).orElseThrow();
        HubResponse<Enrollee> response = enrollmentService.enroll(environmentName, studyShortcode, result.participantUser(), result.portalParticipantUser(), null);
        return new EnrolleeBundle(response.getEnrollee(), result.participantUser(), result.portalParticipantUser(), portal.getId());
    }
}
