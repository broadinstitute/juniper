package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItem;
import bio.terra.pearl.core.model.dataimport.ImportStatus;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.dataimport.ImportFileFormat;
import bio.terra.pearl.core.service.dataimport.ImportItemService;
import bio.terra.pearl.core.service.dataimport.ImportService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.survey.AnswerService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Slf4j
public class EnrolleeImportServiceTests extends BaseSpringBootTest {
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private AdminUserFactory adminUserFactory;
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
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private ImportService importService;
    @Autowired
    private ImportItemService importItemService;

    @Test
    @Transactional
    public void testImportEnrolleesCSV(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        AdminUser adminUser = adminUserFactory.builder(getTestName(info)).build();
        AdminUser savedAdmin = adminUserService.create(adminUser);

        String csvString = """
                column1,column2,column3,account.username,account.createdAt,enrollee.createdAt,profile.birthDate
                a,b,c,userName1,"2024-05-09 01:37PM","2024-05-09 01:38PM","1980-10-10"
                x,y,z,userName2,"2024-05-11 10:00AM","2024-05-11 10:00AM"
                """;

        String csvStringUpdate = """
                account.username,enrollee.createdAt,profile.birthDate
                userName1,"2024-05-09 01:38PM","1982-10-10"
                userName2,"2024-05-11 10:00AM","1990-10-10"
                """;

        Import dataImport = enrolleeImportService.importEnrollees(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                new ByteArrayInputStream(csvString.getBytes()),
                savedAdmin.getId(), ImportFileFormat.CSV);

        Import dataImportQueried = importService.find(dataImport.getId()).get();
        assertThat(dataImport, is(dataImportQueried));
        assertThat(dataImport.getStatus(), is(ImportStatus.DONE));
        importItemService.attachImportItems(dataImport);
        List<ImportItem> imports = dataImport.getImportItems();
        assertThat(imports, hasSize(2));
        ParticipantUser user = participantUserService.find(imports.get(0).getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(user.getId(), bundle.getStudyEnv().getId()).orElseThrow();
        assertThat(enrollee.isSubject(), equalTo(true));
        assertThat(user.getUsername(), equalTo("userName1"));
        assertThat(user.getCreatedAt(), equalTo(Instant.parse("2024-05-09T13:37:00Z")));
        assertThat(enrollee.getCreatedAt(), equalTo(Instant.parse("2024-05-09T13:38:00Z")));

        //load profile
        Profile profile = profileService.find(enrollee.getProfileId()).orElseThrow();
        assertThat(profile.getBirthDate(), equalTo(LocalDate.parse("1980-10-10")));

        ParticipantUser user2 = participantUserService.find(imports.get(1).getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrollee2 = enrolleeService.findByParticipantUserIdAndStudyEnvId(user2.getId(), bundle.getStudyEnv().getId()).orElseThrow();
        assertThat(enrollee2.isSubject(), equalTo(true));
        assertThat(user2.getUsername(), equalTo("userName2"));
        assertThat(user2.getCreatedAt(), equalTo(Instant.parse("2024-05-11T10:00:00Z")));
        assertThat(enrollee2.getCreatedAt(), equalTo(Instant.parse("2024-05-11T10:00:00Z")));


        //now try update
        Import dataImportUpd = enrolleeImportService.importEnrollees(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                new ByteArrayInputStream(csvStringUpdate.getBytes()),
                savedAdmin.getId(), ImportFileFormat.CSV);

        ImportItem importItem = dataImportUpd.getImportItems().get(0);
        ParticipantUser userUpdated = participantUserService.find(importItem.getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrolleeUpdated = enrolleeService.findByParticipantUserIdAndStudyEnvId(userUpdated.getId(), bundle.getStudyEnv().getId()).orElseThrow();
        //should be the same participant and enrollee and profile
        assertThat(userUpdated.getId(), equalTo(user.getId()));
        assertThat(enrollee.getId(), equalTo(enrolleeUpdated.getId()));
        assertThat(enrollee.getProfileId(), equalTo(enrolleeUpdated.getProfileId()));
        //load profile and verify that birthDate was updated
        Profile profileUpdated = profileService.find(enrolleeUpdated.getProfileId()).orElseThrow();
        assertThat(profileUpdated.getBirthDate(), equalTo(LocalDate.parse("1982-10-10")));
        //load and verify that new birthDate was inserted for user2
        profileUpdated = profileService.find(enrollee2.getProfileId()).orElseThrow();
        assertThat(profileUpdated.getBirthDate(), equalTo(LocalDate.parse("1990-10-10")));
    }

    @Test
    @Transactional
    public void testImportEnrollees(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

        AdminUser adminUser = adminUserFactory.builder(getTestName(info)).build();
        AdminUser savedAdmin = adminUserService.create(adminUser);

        String tsvString = """
                column1\tcolumn2\tcolumn3\taccount.username
                a\tb\tc\tuserName1
                x\t\tz\tuserName2             
                """;

        Import dataImport = enrolleeImportService.importEnrollees(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                new ByteArrayInputStream(tsvString.getBytes()),
                savedAdmin.getId(), ImportFileFormat.TSV);

        Import dataImportQueried = importService.find(dataImport.getId()).get();
        assertThat(dataImport, is(dataImportQueried));
        assertThat(dataImport.getStatus(), is(ImportStatus.DONE));
        importItemService.attachImportItems(dataImport);
        List<ImportItem> imports = dataImport.getImportItems();
        assertThat(imports, hasSize(2));
        ParticipantUser user = participantUserService.find(imports.get(0).getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(user.getId(), bundle.getStudyEnv().getId()).orElseThrow();
        assertThat(enrollee.isSubject(), equalTo(true));
        assertThat(user.getUsername(), equalTo("userName1"));

        user = participantUserService.find(imports.get(1).getCreatedParticipantUserId()).orElseThrow();
        enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(user.getId(), bundle.getStudyEnv().getId()).orElseThrow();
        assertThat(enrollee.isSubject(), equalTo(true));
        assertThat(user.getUsername(), equalTo("userName2"));
    }

    @Test
    @Transactional
    public void testGenerateImportMaps(TestInfo info) {
        String tsvString = """
                column1\tcolumn2\tcolumn3
                a\tb\tc
                x\t\tz             
                """;
        List<Map<String, String>> imports = enrolleeImportService.generateImportMaps(
                new ByteArrayInputStream(tsvString.getBytes()), ImportFileFormat.TSV);
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

    String TWO_QUESTION_SURVEY_CONTENT = """
            {
            	"title": "The Basics",
            	"showQuestionNumbers": "off",
            	"pages": [{
            		"elements": [{
            			"name": "importFirstName",
            			"type": "text",
            			"title": "First name",
            			"isRequired": true
            		}, {
            			"name": "importFavColors",
            			"type": "checkbox",
            			"title": "What colors do you like?",
            			"isRequired": true,
            			"choices": [{
            				"text": "red",
            				"value": "red"
            			}, {
            				"text": "green",
            				"value": "green"
            			}, {
            				"text": "blue",
            				"value": "blue"
            			}]
            		}]
            	}]
            }""";

    @Test
    @Transactional
    public void testSurveyResponseImport(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest1")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(bundle.getPortal().getId())
                .version(1)
        );
        surveyFactory.attachToEnv(survey, bundle.getStudyEnv().getId(), true);
        String username = "test-%s@test.com".formatted(RandomStringUtils.randomAlphabetic(5));
        Map<String, String> enrolleeMap = Map.of("enrollee.subject", "true", "account.username", username,
                "importTest1.complete", "true",
                "importTest1.lastUpdatedAt", "2023-08-21 05:17AM",
                "importTest1.importFirstName", "Jeff",
                "importTest1.importFavColors", "[\"red\", \"blue\"]");
        Enrollee enrollee = enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions());
        // confirm a task got created for the enrollee, and the task is complete
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getStatus(), equalTo(TaskStatus.COMPLETE));

        List<Answer> answers = answerService.findByEnrolleeAndSurvey(enrollee.getId(), "importTest1");
        assertThat(answers, hasSize(2));
        Answer firstName = answers.stream().filter(answer -> answer.getQuestionStableId().equals("importFirstName"))
                .findFirst().get();
        assertThat(firstName.getStringValue(), equalTo("Jeff"));
        Answer favColor = answers.stream().filter(answer -> answer.getQuestionStableId().equals("importFavColors"))
                .findFirst().get();
        assertThat(favColor.getObjectValue(), equalTo("[\"red\", \"blue\"]"));
    }

}
