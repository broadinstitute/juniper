package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EnrolleeImportServiceTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeImportService enrolleeImportService;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private ProfileService profileService;

    @Test
    @Transactional
    public void testGenerateImportMaps(TestInfo info) {
        String tsvString = """
                column1\tcolumn2\tcolumn3
                a\tb\tc
                x\t\tz             
                """;
        List<Map<String, String>> imports = enrolleeImportService.generateImportMaps(new ByteArrayInputStream(tsvString.getBytes()));
        assertThat(imports, hasSize(2));
        Map<String, String> expectedMap = Map.of("column1", "a", "column2", "b", "column3", "c");
        assertThat(imports.get(0), equalTo(expectedMap));
        expectedMap = Map.of("column1", "x", "column2", "", "column3", "z");
        assertThat(imports.get(1), equalTo(expectedMap));
    }

    @Test
    @Transactional
    public void testBaseEnrolleeImport(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        String username = "test-%s@test.com".formatted(RandomStringUtils.randomAlphabetic(5));
        Map<String, String> enrolleeMap = Map.of("enrollee.subject", "true", "account.username", username);
        enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions());
        ParticipantUser user = participantUserService.findOne(username, bundle.getStudyEnv().getEnvironmentName()).orElseThrow();
        Enrollee enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(user.getId(), bundle.getStudyEnv().getId()).orElseThrow();
        assertThat(enrollee.isSubject(), equalTo(true));
    }

    @Test
    @Transactional
    public void testEnrolleeProfileImport(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        String username = "test-%s@test.com".formatted(RandomStringUtils.randomAlphabetic(5));
        Map<String, String> enrolleeMap = Map.of(
                "account.username", username,
                "profile.givenName", "Alex",
                "profile.birthDate", "1998-05-14",
                "profile.doNotEmailSolicit", "true",
                "profile.mailingAddress.street1", "105 Broadway",
                "profile.mailingAddress.postalCode", "45455");

        Enrollee enrolle = enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions());
        Profile profile = profileService.loadWithMailingAddress(enrolle.getProfileId()).orElseThrow();
        assertThat(profile.getGivenName(), equalTo("Alex"));
        assertThat(profile.getBirthDate(), equalTo(LocalDate.of(1998, 5, 14)));
        assertThat(profile.isDoNotEmailSolicit(), equalTo(true));
        assertThat(profile.getMailingAddress().getStreet1(), equalTo("105 Broadway"));
        assertThat(profile.getMailingAddress().getPostalCode(), equalTo("45455"));
    }



}
