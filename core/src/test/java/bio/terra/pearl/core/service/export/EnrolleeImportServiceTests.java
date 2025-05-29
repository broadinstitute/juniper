package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dataimport.*;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.export.dataimport.ImportFileFormat;
import bio.terra.pearl.core.service.export.dataimport.ImportItemService;
import bio.terra.pearl.core.service.export.dataimport.ImportService;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.*;
import bio.terra.pearl.core.service.survey.AnswerService;
import bio.terra.pearl.core.service.survey.PreEnrollmentResponseService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

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
    private EnrolleeRelationService enrolleeRelationService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private PortalParticipantUserService portalParticipantUserService;
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
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private SurveyResponseService surveyResponseService;
    @Autowired
    private PreEnrollmentResponseService preEnrollmentResponseService;
    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    KitRequestService kitRequestService;

    public DataImportSetUp setup(TestInfo info, String csvString) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info));

        //create survey
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builderWithDependencies(getTestName(info))
                .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"diagnosis\",\"title\":\"What is your diagnosis?\"}]}]}")
                .stableId("medical_history")
                .portalId(bundle.getPortal().getId()));
        ;
        surveyFactory.attachToEnv(survey, bundle.getStudyEnv().getId(), true);

        return DataImportSetUp.builder()
                .bundle(bundle)
                .adminUser(adminUser)
                .csvString(csvString)
                .build();
    }

    private Instant instantFromZone(String timeString, ZoneId zoneId) {
        return Instant.from(
                DateTimeFormatter.ofPattern(ExportFormatUtils.ANALYSIS_DATE_TIME_FORMAT)
                        .withZone(zoneId) // for now do everything in UTC
                        .parse(timeString));
    }

    private Instant instantFromZone(String timeString) {
        return  instantFromZone(timeString, ZoneId.of("America/New_York"));
    }

    @Test
    @Transactional
    public void testImportEnrolleesCSV(TestInfo info) {
        String csvString = """
                account.username,account.createdAt,enrollee.createdAt,profile.birthDate,medical_history.diagnosis
                userName1,"2024-05-09 01:37PM","2024-05-09 01:38PM","1980-10-10","sick"
                userName2,"2024-05-11 10:00AM","2024-05-11 10:00AM"
                """;
        DataImportSetUp setupData = setup(info, csvString);
        Import dataImport = doImport(setupData.bundle, csvString, setupData.adminUser, ImportFileFormat.CSV);
        UUID studyEnvId = setupData.bundle.getStudyEnv().getId();
        List<ImportItem> imports = dataImport.getImportItems();
        verifyImport(dataImport, 2);

        /*create participantUser, enrollee, profile with expected data to assert*/
        Enrollee enrolleeExpected = new Enrollee();
        enrolleeExpected.setCreatedAt(instantFromZone("2024-05-09 01:38PM"));
        ParticipantUser userExpected = new ParticipantUser();
        userExpected.setCreatedAt(instantFromZone("2024-05-09 01:37PM"));
        userExpected.setUsername("userName1");
        Profile profileExpected = new Profile();
        profileExpected.setBirthDate(LocalDate.parse("1980-10-10"));
        // confirm the account name gets copied over
        profileExpected.setContactEmail(userExpected.getUsername());
        verifyParticipant(imports.get(0), studyEnvId, userExpected, enrolleeExpected, profileExpected);

        Enrollee enrolleeExpected2 = new Enrollee();
        enrolleeExpected2.setCreatedAt(instantFromZone("2024-05-11 10:00AM"));
        ParticipantUser userExpected2 = new ParticipantUser();
        userExpected2.setCreatedAt(instantFromZone("2024-05-11 10:00AM"));
        userExpected2.setUsername("userName2");
        Profile profileExpected2 = new Profile();
        profileExpected2.setContactEmail(userExpected2.getUsername());
        verifyParticipant(imports.get(1), studyEnvId, userExpected2, enrolleeExpected2, profileExpected2);
        verifySurveyQuestionAnswer(imports.get(0), "medical_history", "diagnosis", "sick");
    }

    @Test
    @Transactional
    public void testImportEnrolleeUpdateCSV(TestInfo info) {
        String csvString = """
                account.username,account.createdAt,enrollee.createdAt,profile.birthDate,sample_kit.status,sample_kit.createdAt,sample_kit.sentToAddress,sample_kit.kitType,medical_history.diagnosis
                userName1,"2024-05-09 01:37PM","2024-05-09 01:38PM","1980-10-10","SENT","2024-05-19 01:10PM","{""firstName"":""SS"",""lastName"":""LN1"",""street1"":""320 Charles Street"",""city"":""Cambridge"",""state"":""MA"",""postalCode"":""02141"",""country"":""US""}","SALIVA","sick"
                userName2,"2024-05-11 10:00AM","2024-05-11 10:00AM"
                """;
        DataImportSetUp setupData = setup(info, csvString);
        Import dataImport = doImport(setupData.bundle, csvString, setupData.adminUser, ImportFileFormat.CSV);
        UUID studyEnvId = setupData.bundle.getStudyEnv().getId();
        List<ImportItem> imports = dataImport.getImportItems();
        ParticipantUser user = participantUserService.find(imports.get(0).getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(user.getId(), studyEnvId).orElseThrow();

        //verify survey
        verifySurveyQuestionAnswer(imports.get(0), "medical_history", "diagnosis", "sick");

        /*now try update*/
        String csvStringUpdate = """
                account.username,account.createdAt,enrollee.createdAt,profile.birthDate,medical_history.diagnosis
                userName1,"2024-05-09 01:37PM","2024-05-09 01:38PM","1982-10-10","healthy"
                userName2,"2024-05-11 10:00AM","2024-05-11 10:00AM","1990-10-10","not healthy"
                """;
        Import dataImportUpdate = doImport(setupData.bundle, csvStringUpdate, setupData.adminUser, ImportFileFormat.CSV);
        verifyImport(dataImportUpdate, 2);
        /*create participantUser, enrollee, profile with expected data to assert*/
        ParticipantUser userExpected = new ParticipantUser();
        userExpected.setCreatedAt(instantFromZone("2024-05-09 01:37PM"));
        userExpected.setUsername("userName1");
        Enrollee enrolleeExpected = new Enrollee();
        enrolleeExpected.setCreatedAt(instantFromZone("2024-05-09 01:38PM"));
        Profile profileExpected = new Profile();
        profileExpected.setId(enrollee.getProfileId()); //should be same profile
        profileExpected.setBirthDate(LocalDate.parse("1982-10-10"));
        verifyParticipant(imports.get(0), studyEnvId, userExpected, enrolleeExpected, profileExpected);
        verifySurveyQuestionAnswer(dataImportUpdate.getImportItems().get(0), "medical_history", "diagnosis", "healthy");

        //enrollee2
        ParticipantUser user2 = participantUserService.find(imports.get(1).getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrollee2 = enrolleeService.findByParticipantUserIdAndStudyEnvId(user2.getId(), studyEnvId).orElseThrow();
        Enrollee enrolleeExpected2 = new Enrollee();
        enrolleeExpected2.setCreatedAt(instantFromZone("2024-05-11 10:00AM"));
        ParticipantUser userExpected2 = new ParticipantUser();
        userExpected2.setCreatedAt(instantFromZone("2024-05-11 10:00AM"));
        userExpected2.setUsername("userName2");
        Profile profileExpected2 = new Profile();
        profileExpected2.setId(enrollee2.getProfileId()); //should be same profile
        profileExpected2.setBirthDate(LocalDate.parse("1990-10-10"));
        verifyParticipant(imports.get(1), studyEnvId, userExpected2, enrolleeExpected2, profileExpected2);
        verifySurveyQuestionAnswer(dataImportUpdate.getImportItems().get(1), "medical_history", "diagnosis", "not healthy");
    }

    @Test
    @Transactional
    public void testImportEnrolleeSingleKitRequest(TestInfo info) {
        String csvStringSingleKit = """
                account.username,account.createdAt,enrollee.createdAt,profile.birthDate,sample_kit.status,sample_kit.createdAt,sample_kit.sentAt,sample_kit.trackingNumber,sample_kit.sentToAddress,sample_kit.kitType
                userName1,"2024-05-09 01:37PM","2024-05-09 01:38PM","1980-10-10","SENT","2024-05-09 10:10AM","2024-05-19 01:38PM","KITTRACKNUMBER12345","{""firstName"":""SS"",""lastName"":""LN1"",""street1"":""320 Charles Street"",""city"":""Cambridge"",""state"":""MA"",""postalCode"":""02141"",""country"":""US""}","SALIVA"
                userName2,"2024-05-11 10:00AM","2024-05-11 10:00AM"
                """;

        DataImportSetUp setupData = setup(info, csvStringSingleKit);
        Import dataImport = doImport(setupData.bundle, csvStringSingleKit, setupData.adminUser, ImportFileFormat.CSV);
        verifyImport(dataImport, 2);

        List<KitRequestDto> kitRequestDtoList = new ArrayList<>();
        KitType salivaKit = KitType.builder().name("SALIVA").build();
        KitRequestDto kitRequestDto = KitRequestDto.builder()
                .kitType(salivaKit)
                .status(KitRequestStatus.SENT)
                .trackingNumber("KITTRACKNUMBER12345")
                .createdAt(instantFromZone("2024-05-09 10:10AM"))
                .build();
        kitRequestDtoList.add(kitRequestDto);
        verifyKitRequests(dataImport.getImportItems().get(0), kitRequestDtoList);
    }

    @Test
    @Transactional
    public void testImportEnrolleeMultipleKitRequests(TestInfo info) {
        String csvStringMultipleKits = """
                account.username,account.createdAt,enrollee.createdAt,profile.birthDate,sample_kit.status,sample_kit.createdAt,sample_kit.sentAt,sample_kit.trackingNumber,sample_kit.sentToAddress,sample_kit.kitType,medical_history.diagnosis,sample_kit[2].status,sample_kit[2].createdAt,sample_kit[2].sentAt,sample_kit[2].receivedAt,sample_kit[2].trackingNumber,sample_kit[2].sentToAddress,sample_kit[2].kitType
                userName1,"2024-05-09 01:37PM","2024-05-09 01:38PM","1980-10-10","SENT","2024-05-09 10:10AM","2024-05-19 01:38PM","KITTRACKNUMBER_1","{""firstName"":""SS"",""lastName"":""LN1"",""street1"":""320 Charles Street"",""city"":""Cambridge"",""state"":""MA"",""postalCode"":""02141"",""country"":""US""}","SALIVA", "sick","RECEIVED","2024-05-21 11:10AM","2024-05-22 01:38PM","2024-05-25 01:10AM","KITTRACKNUMBER_2","{""firstName"":""SS2"",""street1"":""320 Charles Street"",""city"":""Cambridge""}","SALIVA"
                userName2,"2024-05-11 10:00AM","2024-05-11 10:00AM"
                """;

        DataImportSetUp setupData = setup(info, csvStringMultipleKits);
        List<KitRequestDto> kitRequestDtoList = new ArrayList<>();
        KitType salivaKit = KitType.builder().name("SALIVA").build();
        KitRequestDto kitRequestDto = KitRequestDto.builder()
                .kitType(salivaKit)
                .status(KitRequestStatus.SENT)
                .trackingNumber("KITTRACKNUMBER_1")
                .createdAt(instantFromZone("2024-05-09 10:10AM"))
                .build();
        KitRequestDto kitRequestDto2 = KitRequestDto.builder()
                .kitType(salivaKit)
                .status(KitRequestStatus.RECEIVED)
                .trackingNumber("KITTRACKNUMBER_2")
                .createdAt(instantFromZone("2024-05-21 11:10AM"))
                .build();
        kitRequestDtoList.add(kitRequestDto);
        kitRequestDtoList.add(kitRequestDto2);

        Import dataImport = doImport(setupData.bundle, csvStringMultipleKits, setupData.adminUser, ImportFileFormat.CSV);
        verifyImport(dataImport, 2);
        verifyKitRequests(dataImport.getImportItems().get(0), kitRequestDtoList);
    }

    @Test
    @Transactional
    public void testImportEnrolleeUpdateDiffPortalCSV(TestInfo info) {
        String csvString = """
                column1,column2,column3,account.username,account.createdAt,enrollee.createdAt,profile.birthDate
                a,b,c,userName1,"2024-05-09 01:37PM","2024-05-09 01:38PM","1980-10-10"
                x,y,z,userName2,"2024-05-11 10:00AM","2024-05-11 10:00AM"
                """;
        DataImportSetUp setupData = setup(info, csvString);
        Import dataImport = doImport(setupData.bundle, csvString, setupData.adminUser, ImportFileFormat.CSV);
        UUID studyEnvId = setupData.bundle.getStudyEnv().getId();
        List<ImportItem> imports = dataImport.getImportItems();
        ParticipantUser user = participantUserService.find(imports.get(0).getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(user.getId(), studyEnvId).orElseThrow();

        /*now try update of the same user, different portal*/
        String csvStringPortal2 = """
                account.username,account.createdAt,enrollee.createdAt,profile.birthDate
                userName1,"2024-05-09 01:37PM","2024-05-09 01:38PM","1990-10-10"
                userName2,"2024-05-11 10:00AM","2024-05-11 10:00AM"
                """;
        DataImportSetUp setupData2 = setup(info, csvStringPortal2);
        Import dataImportUpdate = doImport(setupData2.bundle, csvStringPortal2, setupData2.adminUser, ImportFileFormat.CSV);
        verifyImport(dataImportUpdate, 2);
        ImportItem importItem = dataImportUpdate.getImportItems().get(0);
        ParticipantUser userUpd = participantUserService.find(importItem.getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrolleeUpd = enrolleeService.findByParticipantUserIdAndStudyEnvId(userUpd.getId(), studyEnvId).orElseThrow();
        /*create participantUser, enrollee, profile with expected data to assert*/
        ParticipantUser userExpected = new ParticipantUser();
        userExpected.setCreatedAt(instantFromZone("2024-05-09 01:37PM"));
        userExpected.setUsername("userName1");
        Enrollee enrolleeExpected = new Enrollee();
        enrolleeExpected.setCreatedAt(instantFromZone("2024-05-09 01:38PM"));
        Profile profileExpected = new Profile();
        profileExpected.setBirthDate(LocalDate.parse("1990-10-10"));
        verifyParticipant(importItem, setupData2.bundle.getStudyEnv().getId(), userExpected, enrolleeExpected, profileExpected);
        Assertions.assertNotEquals(enrollee.getProfileId(), equalTo(enrolleeUpd.getProfileId())); //should be diff profiles because different portals
    }

    @Test
    @Transactional
    public void testImportEnrollees(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info));

        String tsvString = """
                column1\tcolumn2\tcolumn3\taccount.username
                a\tb\tc\tuserName1
                x\t\tz\tuserName2             
                """;

        Import dataImport = doImport(bundle, tsvString, adminUser, ImportFileFormat.TSV);

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
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        String username = "test-%s@test.com".formatted(RandomStringUtils.randomAlphabetic(5));
        Map<String, String> enrolleeMap = Map.of("enrollee.subject", "true", "account.username", username);
        enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions(), null);
        ParticipantUser user = participantUserService.findOne(username, bundle.getStudyEnv().getEnvironmentName()).orElseThrow();
        Enrollee enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(user.getId(), bundle.getStudyEnv().getId()).orElseThrow();
        assertThat(enrollee.isSubject(), equalTo(true));
    }

    @Test
    @Transactional
    public void testEnrolleeProfileImport(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
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
                new ExportOptions(), null);
        Profile profile = profileService.loadWithMailingAddress(enrolle.getProfileId()).orElseThrow();
        assertThat(profile.getGivenName(), equalTo("Alex"));
        assertThat(profile.getBirthDate(), equalTo(LocalDate.of(1998, 5, 14)));
        assertThat(profile.isDoNotEmailSolicit(), equalTo(true));
        assertThat(profile.getMailingAddress().getStreet1(), equalTo("105 Broadway"));
        assertThat(profile.getMailingAddress().getPostalCode(), equalTo("45455"));
    }

    @Test
    @Transactional
    public void testImportPreEnroll(TestInfo info) throws JsonProcessingException {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

        Survey preEnroll = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("preEnroll")
                .content("{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"name\",\"title\":\"What is your name?\"}]}]}")
                .surveyType(SurveyType.PRE_ENROLL)
                .autoAssign(false)
                .portalId(bundle.getPortal().getId()));

        surveyFactory.attachToEnv(preEnroll, bundle.getStudyEnv().getId(), true);

        String username = "test-%s@test.com".formatted(RandomStringUtils.randomAlphabetic(5));
        Map<String, String> enrolleeMap = Map.of(
                "account.username", username,
                "preEnroll.name", "Alex",
                "profile.givenName", "Alex",
                "profile.birthDate", "1998-05-14",
                "profile.doNotEmailSolicit", "true",
                "profile.mailingAddress.street1", "105 Broadway",
                "profile.mailingAddress.postalCode", "45455");

        Enrollee enrollee = enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions(), null);

        List<SurveyResponse> responses = surveyResponseService.findByEnrolleeId(enrollee.getId());

        assertThat(responses, hasSize(1));
        SurveyResponse response = responses.get(0);

        assertThat(response.getSurveyId(), equalTo(preEnroll.getId()));

        List<Answer> answers = answerService.findByEnrolleeAndSurvey(enrollee.getId(), preEnroll.getStableId());
        assertThat(answers, hasSize(1));

        Answer answer = answers.getFirst();
        assertThat(answer.getQuestionStableId(), equalTo("name"));
        assertThat(answer.getStringValue(), equalTo("Alex"));


        enrollee = enrolleeService.find(enrollee.getId()).orElseThrow();
        assertThat(enrollee.getPreEnrollmentResponseId(), notNullValue());

        PreEnrollmentResponse preEnrollmentResponse = preEnrollmentResponseService.find(enrollee.getPreEnrollmentResponseId()).orElseThrow();

        assertThat(preEnrollmentResponse.getSurveyId(), equalTo(preEnroll.getId()));

        PortalParticipantUser ppUser = portalParticipantUserService.findForEnrollee(enrollee);
        assertThat(preEnrollmentResponse.getPortalParticipantUserId(), equalTo(ppUser.getId()));

        JsonNode node = objectMapper.readTree(preEnrollmentResponse.getFullData());

        assertThat(node.isArray(), equalTo(true));
        assertThat(node.size(), equalTo(1));

        JsonNode questionNode = node.get(0);

        assertThat(questionNode.get("questionStableId").asText(), equalTo("name"));
        assertThat(questionNode.get("stringValue").asText(), equalTo("Alex"));

        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());

        assertThat(tasks, hasSize(0)); // does not create a task for pre-enrollment survey responses

    }


    /** check that imports won't overwrite previously entered profiles in a multi-study setting */
    @Test
    @Transactional
    public void testEnrolleeProfileImportDoesntOverwrite(TestInfo info) {
        StudyEnvironmentBundle study1Bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        StudyEnvironmentBundle study2Bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb,
                study1Bundle.getPortal(), study1Bundle.getPortalEnv());
        Profile existingProfile = Profile.builder()
                .givenName("John")
                .birthDate(LocalDate.of(1989, 1, 1))
                .mailingAddress(MailingAddress.builder()
                        .street1("123 Main St")
                        .postalCode("12345")
                        .build())
                .build();
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), study1Bundle.getPortalEnv(), study1Bundle.getStudyEnv(), existingProfile);
        String username = enrolleeBundle.participantUser().getUsername();
        Map<String, String> enrolleeMap = Map.of(
                "account.username", username,
                "profile.familyName", "Smith");

        Enrollee importedEnrolle = enrolleeImportService.importEnrollee(
                study2Bundle.getPortal().getShortcode(),
                study2Bundle.getStudy().getShortcode(),
                study2Bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions(), null);
        Profile profile = profileService.loadWithMailingAddress(importedEnrolle.getProfileId()).orElseThrow();
        assertThat(profile.getGivenName(), equalTo("John"));
        assertThat(profile.getFamilyName(), equalTo("Smith"));
        assertThat(profile.getBirthDate(), equalTo(LocalDate.of(1989, 1, 1)));
        assertThat(profile.getMailingAddress().getStreet1(), equalTo("123 Main St"));
        assertThat(profile.getMailingAddress().getPostalCode(), equalTo("12345"));

        Enrollee priorEnrollee = enrolleeService.find(enrolleeBundle.enrollee().getId()).orElseThrow();
        assertThat(priorEnrollee.getProfileId(), equalTo(importedEnrolle.getProfileId()));
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
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest1")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(bundle.getPortal().getId())
                        .answerMappings(List.of(
                                AnswerMapping.builder()
                                        .targetType(AnswerMappingTargetType.PROFILE)
                                        .mapType(AnswerMappingMapType.STRING_TO_STRING)
                                        .targetField("givenName")
                                        .questionStableId("importFirstName")
                                        .build()
                        ))
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
                new ExportOptions(), null);
        // confirm a task got created for the enrollee, and the task is complete
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getStatus(), equalTo(TaskStatus.COMPLETE));
        List<SurveyResponse> responses = surveyResponseService.findByEnrolleeId(enrollee.getId());
        assertThat(responses, hasSize(1));
        assertThat(responses.get(0).isComplete(), equalTo(true));

        List<Answer> answers = answerService.findByEnrolleeAndSurvey(enrollee.getId(), "importTest1");
        assertThat(answers, hasSize(2));
        Answer firstName = answers.stream().filter(answer -> answer.getQuestionStableId().equals("importFirstName"))
                .findFirst().get();
        assertThat(firstName.getStringValue(), equalTo("Jeff"));
        Answer favColor = answers.stream().filter(answer -> answer.getQuestionStableId().equals("importFavColors"))
                .findFirst().get();
        assertThat(favColor.getObjectValue(), equalTo("[\"red\", \"blue\"]"));

        // confirm profile mapping got processed
        Profile profile = profileService.find(enrollee.getProfileId()).orElseThrow();
        assertThat(profile.getGivenName(), equalTo("Jeff"));
    }

    @Test
    @Transactional
    public void testSurveyResponseImportDefaultComplete(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest1")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(bundle.getPortal().getId())
                .answerMappings(List.of(
                        AnswerMapping.builder()
                                .targetType(AnswerMappingTargetType.PROFILE)
                                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                                .targetField("givenName")
                                .questionStableId("importFirstName")
                                .build()
                ))
                .version(1)
        );
        surveyFactory.attachToEnv(survey, bundle.getStudyEnv().getId(), true);
        String username = "test-%s@test.com".formatted(RandomStringUtils.randomAlphabetic(5));
        Map<String, String> enrolleeMap = Map.of("enrollee.subject", "true", "account.username", username,
                "importTest1.lastUpdatedAt", "2023-08-21 05:17AM",
                "importTest1.importFirstName", "Jeff",
                "importTest1.importFavColors", "[\"red\", \"blue\"]");
        Enrollee enrollee = enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions(), null);
        // confirm a task got created for the enrollee, and the task is complete
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getStatus(), equalTo(TaskStatus.COMPLETE));
        List<SurveyResponse> responses = surveyResponseService.findByEnrolleeId(enrollee.getId());
        assertThat(responses, hasSize(1));
        assertThat(responses.get(0).isComplete(), equalTo(true));

        // now an enrollee with unspecified complete, but no answers for the survey
        enrolleeMap = Map.of("enrollee.subject", "true", "account.username", username + "2",
                "importTest1.lastUpdatedAt", "",
                "importTest1.importFirstName", "",
                "importTest1.importFavColors", "");
        enrollee = enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions(), null);
        // confirm a task got created for the enrollee, and the task is NOT complete
        tasks = participantTaskService.findByEnrolleeId(enrollee.getId());
        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0).getStatus(), equalTo(TaskStatus.NEW));
        responses = surveyResponseService.findByEnrolleeId(enrollee.getId());
        assertThat(responses, hasSize(0));
    }

    @Test
    @Transactional
    public void testAccountImport(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

        String username = "test-%s@test.com".formatted(RandomStringUtils.randomAlphabetic(5));

        Map<String, String> enrolleeMap = Map.of(
                "account.username", username,
                "profile.givenName", "Alex");

        EnrolleeImportService.AccountImportData accountImportData = new EnrolleeImportService.AccountImportData();
        accountImportData.setEnrolleeData(enrolleeMap);
        accountImportData.setEmail(username);

        List<Enrollee> enrollees = importAccount(info, accountImportData, bundle);

        assertThat(enrollees, hasSize(1));
        Enrollee enrollee = enrollees.getFirst();

        Profile profile = profileService.loadWithMailingAddress(enrollee.getProfileId()).orElseThrow();
        assertThat(profile.getGivenName(), equalTo("Alex"));
    }

    @Test
    @Transactional
    public void testAccountImportWithProxies(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

        List<Map<String, String>> proxies = List.of(
                Map.of("proxy.username", "proxy@test.com", "profile.givenName", "Proxy1"),
                Map.of("proxy.username", "proxy@test.com", "profile.givenName", "Proxy2"),
                Map.of("proxy.username", "proxy@test.com", "profile.givenName", "Proxy3"));

        String username = "proxy@test.com";

        EnrolleeImportService.AccountImportData accountImportData = new EnrolleeImportService.AccountImportData();

        accountImportData.setProxyData(proxies);
        accountImportData.setEmail(username);

        List<Enrollee> enrollees = importAccount(info, accountImportData, bundle);

        assertThat(enrollees, hasSize(4));

        List<Enrollee> nonSubject = enrollees.stream().filter(enrollee -> !enrollee.isSubject()).toList();
        assertThat(nonSubject, hasSize(1));

        Enrollee proxy = nonSubject.get(0);

        List<Enrollee> proxiesList = enrollees.stream().filter(Enrollee::isSubject).toList();
        assertThat(proxiesList, hasSize(3));

        Set<String> names = proxiesList.stream().map(Enrollee::getProfileId).map(profileId -> {
            Profile profile = profileService.loadWithMailingAddress(profileId).orElseThrow();
            return profile.getGivenName();
        }).collect(Collectors.toSet());

        assertEquals(Set.of("Proxy1", "Proxy2", "Proxy3"), names);

        List<EnrolleeRelation> relations = enrolleeRelationService.findAllByEnrolleeId(proxy.getId());

        assertThat(relations, hasSize(3));

        assertTrue(relations.stream().allMatch(relation -> relation.getEnrolleeId().equals(proxy.getId())));

        Set<UUID> proxyIds = relations.stream().map(EnrolleeRelation::getTargetEnrolleeId).collect(Collectors.toSet());
        Set<UUID> expectedProxyIds = proxiesList.stream().map(Enrollee::getId).collect(Collectors.toSet());

        assertEquals(expectedProxyIds, proxyIds);

    }

    @Test
    @Transactional
    public void testSelfEnrolledWithProxies(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);

        List<Map<String, String>> proxies = List.of(
                Map.of("proxy.username", "proxy@test.com", "profile.givenName", "Peter"),
                Map.of("proxy.username", "proxy@test.com", "profile.givenName", "Jonathan"));

        Map<String, String> enrolleeMap = Map.of(
                "account.username", "proxy@test.com",
                "profile.givenName", "Jonas");

        EnrolleeImportService.AccountImportData accountImportData = new EnrolleeImportService.AccountImportData();

        accountImportData.setProxyData(proxies);
        accountImportData.setEnrolleeData(enrolleeMap);
        accountImportData.setEmail("proxy@test.com");

        List<Enrollee> enrollees = importAccount(info, accountImportData, bundle);

        assertThat(enrollees, hasSize(3));

        assertTrue(enrollees.stream().allMatch(Enrollee::isSubject));

        Set<String> names = enrollees.stream().map(Enrollee::getProfileId).map(profileId -> {
            Profile profile = profileService.loadWithMailingAddress(profileId).orElseThrow();
            return profile.getGivenName();
        }).collect(Collectors.toSet());

        assertEquals(Set.of("Peter", "Jonathan", "Jonas"), names);
    }

    @Test
    public void testGroupByAccountProxies(TestInfo info) {
        List<EnrolleeImportService.AccountImportData> accountImportData = enrolleeImportService.groupImportMapsByAccount(
                List.of(
                        Map.of("account.username", "proxy@test.com", "profile.givenName", "John"),
                        Map.of("proxy.username", "proxy@test.com", "profile.givenName", "Jane"),
                        Map.of("proxy.username", "proxy@test.com", "profile.givenName", "Jim"),
                        Map.of("account.username", "normal_user@test.com", "profile.givenName", "Jack"),
                        Map.of("proxy.username", "different_proxy@test.com", "profile.givenName", "Jill")
                )
        );

        assertThat(accountImportData, hasSize(3));

        EnrolleeImportService.AccountImportData proxy = accountImportData.stream()
                .filter(data -> data.getEmail().equals("proxy@test.com")).findFirst().orElseThrow();

        assertEquals(proxy.getEnrolleeData().get("profile.givenName"), "John");

        List<Map<String, String>> proxyData = proxy.getProxyData();
        assertThat(proxyData, hasSize(2));

        Set<String> names = proxyData.stream().map(data -> data.get("profile.givenName")).collect(Collectors.toSet());
        assertEquals(Set.of("Jane", "Jim"), names);

        EnrolleeImportService.AccountImportData normalUser = accountImportData.stream()
                .filter(data -> data.getEmail().equals("normal_user@test.com")).findFirst().orElseThrow();

        assertEquals(normalUser.getEnrolleeData().get("profile.givenName"), "Jack");
        assertEquals(0, normalUser.getProxyData().size());

        EnrolleeImportService.AccountImportData diffProxyUser = accountImportData.stream()
                .filter(data -> data.getEmail().equals("different_proxy@test.com")).findFirst().orElseThrow();

        assertNull(diffProxyUser.getEnrolleeData());

        assertEquals(diffProxyUser.getProxyData().get(0).get("profile.givenName"), "Jill");

    }

    @Test
    @Transactional
    public void testImportSurveyResponseTime(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest1")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(bundle.getPortal().getId())
                .version(1)
        );
        surveyFactory.attachToEnv(survey, bundle.getStudyEnv().getId(), true);
        String username = "user@asdfasdfasdf.com";
        Map<String, String> enrolleeMap = Map.of("enrollee.subject", "true", "account.username", username,
                "importTest1.complete", "true",
                "importTest1.completedAt", "2023-08-22 05:17AM",
                "importTest1.createdAt", "2023-08-20 05:17AM",
                "importTest1.lastUpdatedAt", "2023-08-21 05:17AM",
                "importTest1.importFirstName", "Jeff",
                "importTest1.importFavColors", "[\"red\", \"blue\"]");

        Enrollee enrollee = enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions(), null);

        List<SurveyResponse> responses = surveyResponseService.findByEnrolleeId(enrollee.getId());

        assertThat(responses, hasSize(1));

        SurveyResponse response = responses.get(0);

        assertThat(response.getCreatedAt(), equalTo(instantFromZone("2023-08-20 05:17AM")));
        assertThat(response.getLastUpdatedAt(), equalTo(instantFromZone("2023-08-21 05:17AM")));

        PortalParticipantUser ppUser = portalParticipantUserService.findForEnrollee(enrollee);

        ParticipantTask task = participantTaskService.findTaskForActivity(ppUser.getId(), bundle.getStudyEnv().getId(), survey.getStableId()).orElseThrow();
        assertThat(task.getCompletedAt(), equalTo(instantFromZone("2023-08-22 05:17AM")));
    }

    @Test
    @Transactional
    public void testImportMultipleSurveyResponses(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest1")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(bundle.getPortal().getId())
                .version(1)
        );
        surveyFactory.attachToEnv(survey, bundle.getStudyEnv().getId(), true);
        String username = "test@test.com";

        Map<String, String> enrolleeMap = Map.of("enrollee.subject", "true", "account.username", username,
                "importTest1.complete", "true",
                "importTest1.importFirstName", "Jeff",
                "importTest1.importFavColors", "[\"red\", \"blue\"]",
                "importTest1.createdAt", "2023-08-21 05:17AM",
                "importTest1[2].complete", "true",
                "importTest1[2].importFirstName", "Jeffrey",
                "importTest1[2].importFavColors", "[\"green\"]",
                "importTest1[2].createdAt", "2023-08-20 05:17AM");

        Enrollee enrollee = enrolleeImportService.importEnrollee(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions(), null);

        List<SurveyResponse> responses = surveyResponseService.findByEnrolleeId(enrollee.getId());

        assertThat(responses, hasSize(2));

        responses.sort(Comparator.comparing(SurveyResponse::getLastUpdatedAt));

        SurveyResponse previousResponse = surveyResponseService.findOneWithAnswers(responses.get(0).getId()).orElseThrow();
        SurveyResponse latestResponse = surveyResponseService.findOneWithAnswers(responses.get(1).getId()).orElseThrow();

        assertThat(previousResponse.getSurveyId(), equalTo(survey.getId()));
        assertThat(previousResponse.isComplete(), equalTo(true));
        assertThat(previousResponse.getLastUpdatedAt(), equalTo(instantFromZone("2023-08-20 05:17AM")));
        assertThat(previousResponse.getAnswers().stream().filter(answer -> answer.getQuestionStableId().equals("importFirstName"))
                .findFirst().get().getStringValue(), equalTo("Jeffrey"));

        assertThat(latestResponse.getSurveyId(), equalTo(survey.getId()));
        assertThat(latestResponse.isComplete(), equalTo(true));
        assertThat(latestResponse.getLastUpdatedAt(), equalTo(instantFromZone("2023-08-21 05:17AM")));
        assertThat(latestResponse.getAnswers().stream().filter(answer -> answer.getQuestionStableId().equals("importFirstName"))
                .findFirst().get().getStringValue(), equalTo("Jeff"));


    }

    @Test
    @Transactional
    public void testImportAndReimportMultipleSurveyResponses(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest1")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(studyEnvBundle.getPortal().getId())
                .autoAssign(true)
                .version(1)
        );
        surveyFactory.attachToEnv(survey, studyEnvBundle.getStudyEnv().getId(), true);
        String username = "asdf@asdf.com";

        String oldCompletionDateStr = "2023-08-20 05:17AM";
        String latestCompletionDateStr = "2023-08-24 05:17AM";
        Instant oldCompletionDate = instantFromZone("2023-08-20 05:17AM");
        Instant latestCompletionDate = instantFromZone("2023-08-24 05:17AM");

        EnrolleeBundle enrolleeBundle = enrolleeFactory.enroll(
                username,
                studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(),
                studyEnvBundle.getStudyEnv().getEnvironmentName());

        // IMPORT 1 - update latest completion, add old response
        Map<String, String> enrolleeMap1 = Map.of("enrollee.subject", "true", "account.username", username,
                "importTest1.complete", "true",
                "importTest1.importFirstName", "Jeff",
                "importTest1.importFavColors", "[\"red\", \"blue\"]",
                "importTest1.createdAt", latestCompletionDateStr,
                "importTest1[2].complete", "true",
                "importTest1[2].importFirstName", "Jeffrey",
                "importTest1[2].importFavColors", "[\"green\"]",
                "importTest1[2].createdAt", oldCompletionDateStr);

        Enrollee enrollee = enrolleeImportService.importEnrollee(
                studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(),
                studyEnvBundle.getStudyEnv(),
                enrolleeMap1,
                new ExportOptions(), null);

        List<SurveyResponse> importedResponses = surveyResponseService.findByEnrolleeId(enrollee.getId()).stream().map(response -> surveyResponseService.findOneWithAnswers(response.getId()).orElseThrow()).sorted(Comparator.comparing(SurveyResponse::getCreatedAt)).collect(Collectors.toList());
        List<ParticipantTask> importedTasks = participantTaskService.findAllTasksForActivity(enrolleeBundle.portalParticipantUser().getId(), studyEnvBundle.getStudyEnv().getId(), survey.getStableId()).stream().sorted(Comparator.comparing(ParticipantTask::getCompletedAt)).collect(Collectors.toList());

        assertThat(importedResponses, hasSize(2));
        assertThat(importedTasks, hasSize(2));

        SurveyResponse importedOldResponse = importedResponses.get(0);
        ParticipantTask importedOldTask = importedTasks.get(0);

        assertThat(importedOldResponse.getCreatedAt(), equalTo(oldCompletionDate));
        assertThat(importedOldTask.getSurveyResponseId(), equalTo(importedOldResponse.getId()));
        assertThat(importedOldTask.getCompletedAt(), equalTo(oldCompletionDate));
        assertThat(importedOldResponse.getAnswers().size(), equalTo(2));
        assertThat(importedOldResponse.getAnswers().stream().map(Answer::valueAsString).collect(Collectors.toSet()), equalTo(Set.of("Jeffrey", "[\"green\"]")));

        SurveyResponse importedLatestResponse = importedResponses.get(1);
        ParticipantTask importedLatestTask = importedTasks.get(1);

        assertThat(importedLatestResponse.getCreatedAt(), equalTo(latestCompletionDate));
        assertThat(importedLatestTask.getCompletedAt(), equalTo(latestCompletionDate));
        assertThat(importedLatestTask.getSurveyResponseId(), equalTo(importedLatestResponse.getId()));
        assertThat(importedLatestResponse.getAnswers().size(), equalTo(2));
        assertThat(importedLatestResponse.getAnswers().stream().map(Answer::valueAsString).collect(Collectors.toSet()), equalTo(Set.of("Jeff", "[\"red\", \"blue\"]")));


        // IMPORT 2 - replace answers from import 1
        Map<String, String> enrolleeMap2 = Map.of("enrollee.subject", "true", "account.username", username,
                "importTest1.complete", "true",
                "importTest1.importFirstName", "Alex",
                "importTest1.importFavColors", "[\"aquamarine\", \"purple\"]",
                "importTest1.createdAt", latestCompletionDateStr,
                "importTest1[2].complete", "true",
                "importTest1[2].importFirstName", "Alexander",
                "importTest1[2].importFavColors", "[\"orange\"]",
                "importTest1[2].createdAt", oldCompletionDateStr);

        enrollee = enrolleeImportService.importEnrollee(
                studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(),
                studyEnvBundle.getStudyEnv(),
                enrolleeMap2,
                new ExportOptions(), null);

        importedResponses = surveyResponseService.findByEnrolleeId(enrollee.getId()).stream().map(response -> surveyResponseService.findOneWithAnswers(response.getId()).orElseThrow()).sorted(Comparator.comparing(SurveyResponse::getCreatedAt)).collect(Collectors.toList());
        importedTasks = participantTaskService.findAllTasksForActivity(enrolleeBundle.portalParticipantUser().getId(), studyEnvBundle.getStudyEnv().getId(), survey.getStableId()).stream().sorted(Comparator.comparing(ParticipantTask::getCompletedAt)).collect(Collectors.toList());

        assertThat(importedResponses, hasSize(2));
        assertThat(importedTasks, hasSize(2));

        importedOldResponse = importedResponses.get(0);
        importedOldTask = importedTasks.get(0);

        assertThat(importedOldResponse.getCreatedAt(), equalTo(oldCompletionDate));
        assertThat(importedOldTask.getCompletedAt(), equalTo(oldCompletionDate));
        assertThat(importedOldTask.getSurveyResponseId(), equalTo(importedOldResponse.getId()));
        assertThat(importedOldResponse.getAnswers().size(), equalTo(2));
        assertThat(importedOldResponse.getAnswers().stream().map(Answer::valueAsString).collect(Collectors.toSet()), equalTo(Set.of("Alexander", "[\"orange\"]")));

        importedLatestResponse = importedResponses.get(1);
        importedLatestTask = importedTasks.get(1);

        assertThat(importedLatestResponse.getCreatedAt(), equalTo(latestCompletionDate));
        assertThat(importedLatestTask.getCompletedAt(), equalTo(latestCompletionDate));
        assertThat(importedLatestTask.getSurveyResponseId(), equalTo(importedLatestResponse.getId()));
        assertThat(importedLatestResponse.getAnswers().size(), equalTo(2));
        assertThat(importedLatestResponse.getAnswers().stream().map(Answer::valueAsString).collect(Collectors.toSet()), equalTo(Set.of("Alex", "[\"aquamarine\", \"purple\"]")));
    }

    @Test
    @Transactional
    public void testCompletedAtOnlyInferredIfCompleted(TestInfo info) {
        StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        Survey survey1 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest1")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(studyEnvBundle.getPortal().getId())
                .autoAssign(true)
                .version(1)
        );
        surveyFactory.attachToEnv(survey1, studyEnvBundle.getStudyEnv().getId(), true);

        Survey survey2 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest2")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(studyEnvBundle.getPortal().getId())
                .autoAssign(true)
                .version(1)
        );
        surveyFactory.attachToEnv(survey2, studyEnvBundle.getStudyEnv().getId(), true);

        Survey survey3 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest3")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(studyEnvBundle.getPortal().getId())
                .autoAssign(true)
                .version(1)
        );
        surveyFactory.attachToEnv(survey3, studyEnvBundle.getStudyEnv().getId(), true);

        Survey survey4 = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(info))
                .stableId("importTest4")
                .content(TWO_QUESTION_SURVEY_CONTENT)
                .portalId(studyEnvBundle.getPortal().getId())
                .autoAssign(true)
                .version(1)
        );
        surveyFactory.attachToEnv(survey4, studyEnvBundle.getStudyEnv().getId(), true);


        Map<String, String> enrolleeMap = Map.ofEntries(
                Map.entry("enrollee.subject", "true"),
                Map.entry("account.username", "test@test.com"),
                Map.entry("importTest1.complete", "true"), // 1: completed, no completedat but has createdat
                Map.entry("importTest1.importFirstName", "Jeff"),
                Map.entry("importTest1.importFavColors", "[\"red\", \"blue\"]"),
                Map.entry("importTest1.createdAt", "2023-08-21 05:17AM"),
                Map.entry("importTest2.complete", "false"), // 2: incomplete, no completedat
                Map.entry("importTest2.importFirstName", "Jeffrey"),
                Map.entry("importTest2.importFavColors", "[\"green\"]"),
                Map.entry("importTest2.createdAt", "2023-08-20 05:17AM"),
                Map.entry("importTest3.complete", "true"), // 3: completed, has completedat
                Map.entry("importTest3.importFirstName", "Alex"),
                Map.entry("importTest3.importFavColors", "[\"aquamarine\", \"purple\"]"),
                Map.entry("importTest3.createdAt", "2023-08-23 05:17AM"),
                Map.entry("importTest3.completedAt", "2023-08-22 05:17AM"),
                Map.entry("importTest4.complete", "true"), // 4: completed, no completedat no createdat
                Map.entry("importTest4.importFirstName", "Alex"),
                Map.entry("importTest4.importFavColors", "[\"orange\"]")
        );

        Enrollee enrollee = enrolleeImportService.importEnrollee(
                studyEnvBundle.getPortal().getShortcode(),
                studyEnvBundle.getStudy().getShortcode(),
                studyEnvBundle.getStudyEnv(),
                enrolleeMap,
                new ExportOptions(), null);


        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(enrollee.getId());

        assertThat(tasks, hasSize(4));

        ParticipantTask task1 = tasks.stream().filter(task -> task.getTargetStableId().equals(survey1.getStableId())).findFirst().orElseThrow();
        assertThat(task1.getCompletedAt(), equalTo(instantFromZone("2023-08-21 05:17AM")));
        assertThat(task1.getStatus(), equalTo(TaskStatus.COMPLETE));

        ParticipantTask task2 = tasks.stream().filter(task -> task.getTargetStableId().equals(survey2.getStableId())).findFirst().orElseThrow();
        assertThat(task2.getCompletedAt(), nullValue());
        assertThat(task2.getStatus(), equalTo(TaskStatus.IN_PROGRESS));

        ParticipantTask task3 = tasks.stream().filter(task -> task.getTargetStableId().equals(survey3.getStableId())).findFirst().orElseThrow();
        assertThat(task3.getCompletedAt(), equalTo(instantFromZone("2023-08-22 05:17AM")));
        assertThat(task3.getStatus(), equalTo(TaskStatus.COMPLETE));

        ParticipantTask task4 = tasks.stream().filter(task -> task.getTargetStableId().equals(survey4.getStableId())).findFirst().orElseThrow();
        assertTrue(Duration.between(Instant.now(), task4.getCompletedAt()).toSeconds() < 60); // task4 should be completed recently
        assertThat(task4.getStatus(), equalTo(TaskStatus.COMPLETE));

    }

    private void verifyParticipant(ImportItem importItem, UUID studyEnvId,
                                   ParticipantUser userExpected, Enrollee enrolleeExpected, Profile profileExpected) {

        ParticipantUser user = participantUserService.find(importItem.getCreatedParticipantUserId()).orElseThrow();
        Enrollee enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(user.getId(), studyEnvId).orElseThrow();
        assertThat(enrollee.isSubject(), equalTo(true));
        assertThat(user.getUsername(), equalTo(userExpected.getUsername()));
        assertThat(user.getCreatedAt(), equalTo(userExpected.getCreatedAt()));
        assertThat(enrollee.getCreatedAt(), equalTo(enrolleeExpected.getCreatedAt()));
        assertThat(enrollee.getSource(), equalTo(EnrolleeSourceType.IMPORT));

        //load profile
        Profile profile = profileService.find(enrollee.getProfileId()).orElseThrow();
        assertThat(profile.getBirthDate(), equalTo(profileExpected.getBirthDate()));
        if (profileExpected.getId() != null) {
            assertThat(profile.getId(), equalTo(profileExpected.getId()));
        }
    }

    private void verifyImport(Import dataImport, int importItemCount) {
        Import dataImportQueried = importService.find(dataImport.getId()).get();
        assertThat(dataImport, is(dataImportQueried));
        assertThat(dataImport.getStatus(), is(ImportStatus.DONE));
        importItemService.attachImportItems(dataImport);
        List<ImportItem> imports = dataImport.getImportItems();
        assertThat(imports, hasSize(importItemCount));
        long enrolleeCount = imports.stream().filter(importItem -> importItem.getCreatedEnrolleeId() != null).count();
        assertThat(enrolleeCount, equalTo(Long.valueOf(importItemCount)));
    }

    private Import doImport(StudyEnvironmentBundle bundle, String csvString, AdminUser admin, ImportFileFormat fileType) {
        return enrolleeImportService.importEnrollees(
                bundle.getPortal().getShortcode(),
                bundle.getStudy().getShortcode(),
                bundle.getStudyEnv(),
                new ByteArrayInputStream(csvString.getBytes()),
                admin.getId(), fileType);
    }

    private void verifySurveyQuestionAnswer(ImportItem importItem, String surveyStableId, String questionStableId, String questionAnswer) {
        List<ParticipantTask> tasks = participantTaskService.findByEnrolleeId(importItem.getCreatedEnrolleeId());
        assertThat(tasks, hasSize(1));
        List<Answer> answers = answerService.findByEnrolleeAndSurvey(importItem.getCreatedEnrolleeId(), surveyStableId);
        assertThat(answers, hasSize(1));
        Answer thisAnswer = answers.stream().filter(answer -> answer.getQuestionStableId().equals(questionStableId))
                .findFirst().get();
        assertThat(thisAnswer.getStringValue(), equalTo(questionAnswer));
    }

    private void verifyKitRequests(ImportItem importItem, List<KitRequestDto> expectedKitRequests) {
        List<KitRequestDto> kitRequestDtos = kitRequestService.findByEnrollee(enrolleeService.find(importItem.getCreatedEnrolleeId()).get());
        assertThat(kitRequestDtos.size(), equalTo(expectedKitRequests.size()));

        kitRequestDtos.sort(comparing(KitRequestDto::getTrackingNumber));
        expectedKitRequests.sort(comparing(KitRequestDto::getTrackingNumber));

        for (int i = 0; i < expectedKitRequests.size(); i++) {
            KitRequestDto kitRequestDto = kitRequestDtos.get(i);
            KitRequestDto expectedKit = expectedKitRequests.get(i);

            assertThat(kitRequestDto.getKitType().getName(), equalTo(expectedKit.getKitType().getName()));
            assertThat(kitRequestDto.getTrackingNumber(), equalTo(expectedKit.getTrackingNumber()));
            assertThat(kitRequestDto.getStatus(), equalTo(expectedKit.getStatus()));
            assertThat(kitRequestDto.getSentToAddress(), notNullValue());
            assertThat(kitRequestDto.getCreatedAt(), equalTo(expectedKit.getCreatedAt()));
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DataImportSetUp {
        private StudyEnvironmentBundle bundle;
        private AdminUser adminUser;
        private String csvString;
    }

    private List<Enrollee> importAccount(TestInfo info, EnrolleeImportService.AccountImportData accountImportData, StudyEnvironmentBundle bundle) {
        AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info));
        Import dataImport = importService.create(Import.builder().importType(ImportType.PARTICIPANT).studyEnvironmentId(bundle.getStudyEnv().getId()).responsibleUserId(adminUser.getId()).build());
        return enrolleeImportService.importAccount(
                        accountImportData,
                        bundle.getPortal().getShortcode(),
                        bundle.getStudy().getShortcode(),
                        bundle.getStudyEnv(),
                        new ExportOptions(),
                        null,
                        dataImport.getId())
                .stream()
                .map(item -> {
                    if (item.getStatus().equals(ImportItemStatus.FAILED)) {
                        log.debug("Failed to import account: {}", item.getMessage());
                        log.debug(item.getDetail());
                    }
                    return item;
                })
                .filter(importItem -> Objects.nonNull(importItem.getCreatedEnrolleeId()))
                .map(importItem -> enrolleeService.find(importItem.getCreatedEnrolleeId()).orElseThrow())
                .toList();


    }
}
