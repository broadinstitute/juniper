package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.dao.dataimport.TimeShiftDao;
import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.dataimport.*;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.survey.SurveyResponseWithTaskDto;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.export.dataimport.ImportFileFormat;
import bio.terra.pearl.core.service.export.dataimport.ImportItemService;
import bio.terra.pearl.core.service.export.dataimport.ImportService;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.module.*;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.*;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.survey.AnswerProcessingService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.survey.SurveyTaskDispatcher;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskAssignDto;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class EnrolleeImportService {
    private final EnrolleeRelationService enrolleeRelationService;
    private final SurveyService surveyService;
    private final AnswerProcessingService answerProcessingService;
    private final AnswerMappingDao answerMappingDao;

    ExportOptions IMPORT_OPTIONS_TSV = ExportOptions
            .builder()
            .stableIdsForOptions(true)
            .onlyIncludeMostRecent(true)
            .fileFormat(ExportFileFormat.TSV)
            .rowLimit(null)
            .build();

    ExportOptions IMPORT_OPTIONS_CSV = ExportOptions
            .builder()
            .stableIdsForOptions(true)
            .onlyIncludeMostRecent(true)
            .fileFormat(ExportFileFormat.CSV)
            .rowLimit(null)
            .build();

    private final RegistrationService registrationService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeExportService enrolleeExportService;
    private final EnrollmentService enrollmentService;
    private final ProfileService profileService;
    private final SurveyResponseService surveyResponseService;
    private final SurveyTaskDispatcher surveyTaskDispatcher;
    private final ParticipantTaskService participantTaskService;
    private final ParticipantUserService participantUserService;
    private final PortalService portalService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final TimeShiftDao timeShiftDao;
    private final ImportService importService;
    private final ImportItemService importItemService;
    private final KitRequestService kitRequestService;
    private final StudyEnvironmentConfigService studyEnvironmentConfigService;
    private final char CSV_DELIMITER = ',';
    private final char TSV_DELIMITER = '\t';

    public EnrolleeImportService(RegistrationService registrationService, EnrollmentService enrollmentService,
                                 ProfileService profileService, EnrolleeExportService enrolleeExportService,
                                 SurveyResponseService surveyResponseService, ParticipantTaskService participantTaskService, PortalService portalService,
                                 ImportService importService, ImportItemService importItemService, SurveyTaskDispatcher surveyTaskDispatcher,
                                 EnrolleeRelationService enrolleeRelationService,
                                 TimeShiftDao timeShiftDao, EnrolleeService enrolleeService, ParticipantUserService participantUserService,
                                 PortalParticipantUserService portalParticipantUserService, KitRequestService kitRequestService, SurveyService surveyService, AnswerProcessingService answerProcessingService, AnswerMappingDao answerMappingDao, StudyEnvironmentConfigService studyEnvironmentConfigService) {
        this.registrationService = registrationService;
        this.enrollmentService = enrollmentService;
        this.profileService = profileService;
        this.enrolleeExportService = enrolleeExportService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.portalService = portalService;
        this.importService = importService;
        this.importItemService = importItemService;
        this.surveyTaskDispatcher = surveyTaskDispatcher;
        this.timeShiftDao = timeShiftDao;
        this.enrolleeService = enrolleeService;
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.kitRequestService = kitRequestService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.surveyService = surveyService;
        this.answerProcessingService = answerProcessingService;
        this.answerMappingDao = answerMappingDao;
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
    }

    @Transactional
    /**
     * imports the enrollees serialized in the inputstream to the given environment
     */
    public Import importEnrollees(String portalShortcode, String studyShortcode, StudyEnvironment studyEnv, InputStream in,
                                  UUID adminId, ImportFileFormat fileFormat) {
        Import dataImport = Import.builder()
                .responsibleUserId(adminId)
                .studyEnvironmentId(studyEnv.getId())
                .responsibleUserId(adminId)
                .importType(ImportType.PARTICIPANT)
                .status(ImportStatus.PROCESSING)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();
        dataImport = importService.create(dataImport);
        log.info("Started Import ID: {}", dataImport.getId());

        ExportOptions exportOptions = IMPORT_OPTIONS_TSV;
        if (ImportFileFormat.CSV.equals(fileFormat)) {
            exportOptions = IMPORT_OPTIONS_CSV;
        }
        StudyEnvironmentConfig studyEnvConfig = studyEnvironmentConfigService.findByStudyEnvironmentId(studyEnv.getId());
        exportOptions.setTimeZone(studyEnvConfig.getTimeZone());
        List<Map<String, String>> enrolleeMaps = generateImportMaps(in, fileFormat);
        List<AccountImportData> accountData = groupImportMapsByAccount(enrolleeMaps);

        for (AccountImportData account : accountData) {

            List<ImportItem> importItems = importAccount(account, portalShortcode, studyShortcode, studyEnv, exportOptions, adminId, dataImport.getId());

            importItems.forEach(importItem -> {
                log.debug("populated Import Item ID: {}", importItem.getId());
            });
        }
        dataImport.setStatus(ImportStatus.DONE);
        importService.update(dataImport);
        importItemService.attachImportItems(dataImport);
        log.info("Completed importing : {} items for Import ID: {}", dataImport.getImportItems().size(), dataImport.getId());
        return dataImport;
    }


    // An account can have multiple enrollees. This
    // record groups them together so our import process
    // can create them all at once and link them together.
    //
    // Proxy accounts are determined by the presence of the
    // proxy.email field in the import data.
    @Getter
    @Setter
    public static class AccountImportData {
        private String email;
        private Map<String, String> enrolleeData = null;
        private List<Map<String, String>> proxyData = new ArrayList<>();
    }

    List<AccountImportData> groupImportMapsByAccount(List<Map<String, String>> enrolleeMaps) {
        List<AccountImportData> accountData = new ArrayList<>();

        for (Map<String, String> enrolleeMap : enrolleeMaps) {
            String email = enrolleeMap.get("account.username");
            String proxyEmail = enrolleeMap.get("proxy.username");
            if (StringUtils.isBlank(email)) {
                email = proxyEmail;
            }

            AccountImportData account = findAccount(accountData, email);

            if (account == null) {
                AccountImportData newAccount = new AccountImportData();
                newAccount.setEmail(email);
                if (StringUtils.isBlank(proxyEmail)) {
                    newAccount.setEnrolleeData(enrolleeMap);
                } else {
                    newAccount.getProxyData().add(enrolleeMap);
                }
                accountData.add(newAccount);
            } else {
                if (StringUtils.isBlank(proxyEmail)) {
                    account.setEnrolleeData(enrolleeMap);
                } else {
                    account.getProxyData().add(enrolleeMap);
                }
            }
        }

        return accountData;
    }

    AccountImportData findAccount(List<AccountImportData> accountData, String email) {
        return accountData.stream().filter(account -> account.getEmail().equals(email)).findFirst().orElse(null);
    }

    /**
     * transforms a TSV import input stream into a List of string maps, one map per enrollee
     */
    public List<Map<String, String>> generateImportMaps(InputStream in, ImportFileFormat fileFormat) {
        List<Map<String, String>> importMaps = new ArrayList<>();
        Iterable<CSVRecord> parser;

        try {
            CSVFormat format = fileFormat == ImportFileFormat.TSV ? CSVFormat.TDF : CSVFormat.DEFAULT;

            parser = format.builder().setRecordSeparator('\n').build().parse(new InputStreamReader(in));
        } catch (IOException e) {
            throw new RuntimeException("Error parsing input stream", e);
        }

        List<String> header = new ArrayList<>();
        for (CSVRecord record : parser) {
            if (record.getRecordNumber() == 1) {
                header = record.toList();
                // skip the header row
                continue;
            }

            if (record.size() != 0 && record.get(0).equalsIgnoreCase("shortcode")) {
                // skip subheader row
                continue;
            }

            Map<String, String> enrolleeMap = new HashMap<>();
            for (int i = 0; i < record.size(); i++) {
                enrolleeMap.put(header.get(i), record.get(i));
            }
            importMaps.add(enrolleeMap);
        }

        return importMaps;
    }

    @Transactional
    public List<ImportItem> importAccount(AccountImportData accountData, String portalShortcode, String studyShortcode, StudyEnvironment studyEnv, ExportOptions exportOptions, UUID adminId, UUID importId) {
        DataAuditInfo auditInfo = DataAuditInfo.builder().responsibleAdminUserId(adminId).build();

        List<ImportItem> importItems = new ArrayList<>();

        Enrollee accountEnrollee = null;

        // import primary enrollee or create proxy if no primary enrollee exists
        try {
            if (accountData.getEnrolleeData() != null) {
                accountEnrollee = importEnrollee(portalShortcode, studyShortcode, studyEnv, accountData.getEnrolleeData(), exportOptions, adminId);
                importItems.add(createImportItemFromEnrollee(accountEnrollee, importId));
            } else {
                final RegistrationService.RegistrationResult regResult = registerIfNeeded(
                        portalShortcode,
                        studyEnv,
                        ParticipantUser.builder().username(accountData.getEmail()).build());

                accountEnrollee = createProxyEnrolleeIfNeeded(studyShortcode, studyEnv, regResult, auditInfo);
                importItems.add(createImportItemFromEnrollee(accountEnrollee, importId));
            }
        } catch (Exception e) {
            String message = "%s: %s".formatted(e.getClass().getSimpleName(), e.getMessage());

            importItems.add(createFailedImportItem(importId, message, Arrays.toString(e.getStackTrace()), adminId));
            if (!accountData.getProxyData().isEmpty()) {
                log.warn("failed to import primary enrollee, skipping proxy import for username: {}", accountData.getEmail());
            }
            return importItems;
        }

        // create all proxies & create relationship between them
        for (Map<String, String> proxyData : accountData.getProxyData()) {
            try {
                Map<String, String> proxyDataCopy = new HashMap<>(proxyData);
                proxyDataCopy.put("account.username", registrationService.getGovernedUsername(accountData.getEmail(), studyEnv.getEnvironmentName()));
                Enrollee proxyEnrollee = importEnrollee(portalShortcode, studyShortcode, studyEnv, proxyDataCopy, exportOptions, adminId);

                importItems.add(createImportItemFromEnrollee(proxyEnrollee, importId));

                enrolleeRelationService.create(
                        EnrolleeRelation
                                .builder()
                                .enrolleeId(accountEnrollee.getId())
                                .targetEnrolleeId(proxyEnrollee.getId())
                                .relationshipType(RelationshipType.PROXY)
                                .beginDate(Instant.now())
                                .build(),
                        auditInfo
                );
            } catch (Exception e) {
                importItems.add(createFailedImportItem(importId, e.getMessage(), Arrays.toString(e.getStackTrace()), adminId));
            }
        }

        return importItems;
    }

    @Transactional
    public ImportItem createImportItemFromEnrollee(Enrollee enrollee, UUID importId) {
        ImportItem importItem = ImportItem.builder()
                .createdEnrolleeId(enrollee.getId())
                .importId(importId)
                .createdParticipantUserId(enrollee.getParticipantUserId())
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .status(ImportItemStatus.SUCCESS).build();
        return importItemService.create(importItem);
    }

    @Transactional
    public ImportItem createFailedImportItem(UUID importId, String message, String detail, UUID adminId) {
        ImportItem importItem = ImportItem.builder()
                .importId(importId)
                .createdParticipantUserId(adminId)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .status(ImportItemStatus.FAILED)
                .message(message)
                .detail(detail)
                .build();
        return importItemService.create(importItem);
    }


    @Transactional
    public Enrollee importEnrollee(String portalShortcode, String studyShortcode, StudyEnvironment studyEnv, Map<String, String> enrolleeMap, ExportOptions exportOptions, UUID adminId) {
        /** while importing handle update for existing import
         if same enrolle: update enrollee
         if same participant & same portal.. new enrollee & same profile
         if same participant & different portal.. new enrollee & new profile
         **/

        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "importEnrollee")
        ).build();

        ParticipantUserFormatter participantUserFormatter = new ParticipantUserFormatter(exportOptions);
        final ParticipantUser participantUserInfo = participantUserFormatter.fromStringMap(studyEnv.getId(), enrolleeMap, 1);

        final RegistrationService.RegistrationResult regResult = registerIfNeeded(portalShortcode, studyEnv, participantUserInfo);

        Enrollee enrollee = createEnrolleeIfNeeded(studyShortcode, studyEnv, enrolleeMap, exportOptions, regResult, auditInfo, participantUserInfo);

        /** now update the profile */
        Profile profile = importProfile(enrolleeMap, regResult.profile(), exportOptions, studyEnv, auditInfo);

        /** populate kit_requests */
        importKitRequests(enrolleeMap, adminId, enrollee);

        importSurveyResponses(portalShortcode, enrolleeMap, exportOptions, studyEnv, regResult.participantUser(), regResult.portalParticipantUser(), enrollee, auditInfo);

        /** restore email -- reload the pr5ofile since answermappings may have changed it */
        profile = profileService.find(profile.getId()).orElseThrow();
        profile.setDoNotEmail(false);
        profileService.update(profile, auditInfo);
        return enrollee;
    }

    private void importKitRequests(Map<String, String> enrolleeMap, UUID adminId, Enrollee enrollee) {
        KitRequestFormatter formatter = new KitRequestFormatter(new ExportOptions());
        formatter.listFromStringMap(enrollee.getStudyEnvironmentId(), enrolleeMap).stream().forEach(kitRequestDto -> {
            KitRequest kitRequest = kitRequestService.create(convertKitRequestDto(adminId, enrollee, kitRequestDto));
            timeShiftDao.changeKitCreationTime(kitRequest.getId(), kitRequestDto.getCreatedAt());
        });
    }

    private KitRequest convertKitRequestDto(
            UUID adminUserId,
            Enrollee enrollee,
            KitRequestDto kitRequestDto) {

        KitType kitType = kitRequestService.lookupKitTypeByName(kitRequestDto.getKitType().getName());
        return KitRequest.builder()
                .creatingAdminUserId(adminUserId)
                .enrolleeId(enrollee.getId())
                .status(kitRequestDto.getStatus())
                .sentToAddress(kitRequestDto.getSentToAddress())
                .skipAddressValidation(kitRequestDto.isSkipAddressValidation())
                .kitTypeId(kitType.getId())
                .createdAt(kitRequestDto.getCreatedAt())
                .labeledAt(kitRequestDto.getLabeledAt())
                .sentAt(kitRequestDto.getSentAt())
                .receivedAt(kitRequestDto.getReceivedAt())
                .trackingNumber(kitRequestDto.getTrackingNumber())
                .returnTrackingNumber(kitRequestDto.getReturnTrackingNumber())
                .build();
    }

    private RegistrationService.RegistrationResult registerIfNeeded(String portalShortcode, StudyEnvironment studyEnv, ParticipantUser participantUserInfo) {
        return portalParticipantUserService.findOne(participantUserInfo.getUsername(), portalShortcode, studyEnv.getEnvironmentName())
                .map(ppUser -> new RegistrationService.RegistrationResult(
                        participantUserService.findOne(participantUserInfo.getUsername(),
                                studyEnv.getEnvironmentName()).orElseThrow(() -> new IllegalStateException("Participant User could not be found or for PPUser")),
                        ppUser,
                        profileService.loadWithMailingAddress(ppUser.getProfileId()).orElseThrow(IllegalStateException::new)
                )).orElseGet(() ->
                        registrationService.register(portalShortcode, studyEnv.getEnvironmentName(), participantUserInfo.getUsername(), null, null)
                );
    }

    private @NotNull Enrollee createEnrolleeIfNeeded(String studyShortcode, StudyEnvironment studyEnv, Map<String, String> enrolleeMap, ExportOptions exportOptions,
                                                     RegistrationService.RegistrationResult regResult, DataAuditInfo auditInfo, ParticipantUser participantUserInfo) {
        return enrolleeService.findByParticipantUserIdAndStudyEnvId(regResult.participantUser().getId(), studyEnv.getId()).orElseGet(() -> {
            /** user is not enrolled in this study, so we need to create a new enrollee */
            EnrolleeFormatter enrolleeFormatter = new EnrolleeFormatter(exportOptions);
            Enrollee enrolleeInfo = enrolleeFormatter.fromStringMap(studyEnv.getId(), enrolleeMap, 1);
            /** temporarily update the profile to no emails since they'll receive a special welcome email */
            regResult.profile().setDoNotEmail(true);
            profileService.update(regResult.profile(), auditInfo);

            HubResponse<Enrollee> response = enrollmentService.enroll(regResult.portalParticipantUser(), studyEnv.getEnvironmentName(),
                    studyShortcode, regResult.participantUser(), regResult.portalParticipantUser(),
                    null, enrolleeInfo.isSubject(), EnrolleeSourceType.IMPORT);
            Enrollee newEnrollee = response.getEnrollee();
            //update createdAt
            if (enrolleeInfo.getCreatedAt() != null) {
                timeShiftDao.changeEnrolleeCreationTime(response.getEnrollee().getId(), enrolleeInfo.getCreatedAt());
            }
            if (regResult.participantUser().getCreatedAt() != null) {
                timeShiftDao.changeParticipantAccountCreationTime(response.getEnrollee().getParticipantUserId(), participantUserInfo.getCreatedAt());
            }
            return newEnrollee;
        });
    }

    private @NotNull Enrollee createProxyEnrolleeIfNeeded(String studyShortcode, StudyEnvironment studyEnv, RegistrationService.RegistrationResult registration, DataAuditInfo auditInfo) {

        registration.profile().setDoNotEmail(true);
        profileService.update(registration.profile(), auditInfo);

        Optional<Enrollee> enrollee = enrolleeService.findByParticipantUserIdAndStudyEnvId(registration.participantUser().getId(), studyEnv.getId());

        return enrollee.orElseGet(() -> this.enrollmentService.enroll(registration.portalParticipantUser(), studyEnv.getEnvironmentName(), studyShortcode, registration.participantUser(), registration.portalParticipantUser(), null, false).getEnrollee());
    }

    protected Profile importProfile(Map<String, String> enrolleeMap, Profile registrationProfile,
                                    ExportOptions exportOptions, StudyEnvironment studyEnv, DataAuditInfo auditInfo) {
        ProfileFormatter profileFormatter = new ProfileFormatter(exportOptions);
        Profile importProfile = profileFormatter.fromStringMap(studyEnv.getId(), enrolleeMap, 1);
        // only copy non-null properties -- this avoids overwriting already set values (especially in the case of multi-study imports)
        copyNonNullProperties(importProfile, registrationProfile, List.of("mailingAddress"));
        if (importProfile.getMailingAddress() != null) {
            copyNonNullProperties(importProfile.getMailingAddress(), registrationProfile.getMailingAddress(), List.of());
        }
        // we still don't want to send emails during the import process
        registrationProfile.setDoNotEmail(true);
        return profileService.updateWithMailingAddress(registrationProfile, auditInfo);
    }

    protected List<SurveyResponse> importSurveyResponses(String portalShortcode,
                                                         Map<String, String> enrolleeMap,
                                                         ExportOptions exportOptions,
                                                         StudyEnvironment studyEnv,
                                                         ParticipantUser user,
                                                         PortalParticipantUser ppUser,
                                                         Enrollee enrollee,
                                                         DataAuditInfo auditInfo) {
        List<SurveyFormatter> surveyModules = enrolleeExportService.generateSurveyModules(exportOptions, studyEnv.getId(), List.of());
        List<SurveyResponse> responses = new ArrayList<>();
        UUID portalId = portalService.findOneByShortcode(portalShortcode).orElseThrow().getId();
        Survey preEnroll = studyEnv.getPreEnrollSurveyId() != null ? surveyService.find(studyEnv.getPreEnrollSurveyId()).orElse(null) : null;

        for (SurveyFormatter formatter : surveyModules) {
            List<SurveyResponse> surveyResponses;

            if (preEnroll != null && formatter.getModuleName().equals(preEnroll.getStableId())) {
                surveyResponses = List.of(importPreEnrollResponse(preEnroll, portalId, formatter, enrolleeMap, exportOptions, studyEnv, ppUser, user, enrollee, auditInfo));
            } else {
                surveyResponses = importSurveyResponses(portalId, formatter, enrolleeMap, exportOptions, studyEnv, ppUser, enrollee, auditInfo);
            }

            if (surveyResponses != null) {
                responses.addAll(surveyResponses);
            }
        }
        return responses;
    }

    protected SurveyResponse importPreEnrollResponse(Survey preEnroll,
                                                     UUID portalId,
                                                     SurveyFormatter formatter,
                                                     Map<String, String> enrolleeMap,
                                                     ExportOptions exportOptions,
                                                     StudyEnvironment studyEnv,
                                                     PortalParticipantUser ppUser,
                                                     ParticipantUser participantUser,
                                                     Enrollee enrollee,
                                                     DataAuditInfo auditInfo) {
        SurveyResponse response = formatter.fromStringMap(studyEnv.getId(), enrolleeMap, 1);
        if (response == null) {
            return null;
        }

        response.setEnrolleeId(enrollee.getId());
        response.setCreatingParticipantUserId(ppUser.getParticipantUserId());
        response.setSurveyId(preEnroll.getId());

        SurveyResponse created = surveyResponseService.create(response);

        // process any answers that need to be propagated elsewhere to the data model
        answerProcessingService.processAllAnswerMappings(
                enrollee,
                response.getAnswers(),
                answerMappingDao.findBySurveyId(preEnroll.getId()),
                ppUser,
                new ResponsibleEntity(participantUser),
                auditInfo);

        return created;
    }

    protected List<SurveyResponse> importSurveyResponses(UUID portalId, SurveyFormatter formatter, Map<String, String> enrolleeMap, ExportOptions exportOptions,
                                                  StudyEnvironment studyEnv, PortalParticipantUser ppUser, Enrollee enrollee, DataAuditInfo auditInfo) {
        List<SurveyResponseWithTaskDto> responses = formatter.listFromStringMap(studyEnv.getId(), enrolleeMap);
        if (responses == null || responses.isEmpty()) {
            return List.of();
        }

        List<SurveyResponse> imported = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            SurveyResponseWithTaskDto response = responses.get(i);
            imported.add(importSurveyResponse(ppUser, enrollee, studyEnv, formatter, response, portalId, auditInfo, enrolleeMap, i + 1, exportOptions));
        }

        return imported;
    }

    private SurveyResponse importSurveyResponse(PortalParticipantUser ppUser, Enrollee enrollee, StudyEnvironment studyEnv, SurveyFormatter formatter, SurveyResponseWithTaskDto response, UUID portalId, DataAuditInfo auditInfo, Map<String, String> enrolleeMap, Integer repeatNum, ExportOptions options) {

        ParticipantTask relatedTask = findOrCreateTask(ppUser, enrollee, studyEnv, formatter, response, portalId, auditInfo, enrolleeMap, repeatNum, options.getZoneId());

        SurveyResponse updatedResponse = surveyResponseService.updateResponse(response, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "importSurveyResponse")),
                "Imported", ppUser, enrollee, relatedTask.getId(), portalId).getResponse();


        shiftTime(updatedResponse, relatedTask, formatter, enrolleeMap, repeatNum, options.getZoneId());

        return updatedResponse;
    }

    private ParticipantTask findOrCreateTask(PortalParticipantUser ppUser, Enrollee enrollee, StudyEnvironment studyEnv, SurveyFormatter formatter, SurveyResponseWithTaskDto response, UUID portalId, DataAuditInfo auditInfo, Map<String, String> enrolleeMap, Integer repeatNum, ZoneId zoneId) {

        ParticipantTask relatedTask = findTask(ppUser, studyEnv, formatter, enrolleeMap, repeatNum, zoneId);

        if (relatedTask == null) {
            ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(
                    TaskType.SURVEY,
                    formatter.getModuleName(),
                    null, // latest
                    List.of(enrollee.getId()),
                    false,
                    true,
                    "Imported");

            List<ParticipantTask> tasks = surveyTaskDispatcher.assign(
                    assignDto,
                    studyEnv.getId(),
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleSurveyPublished.assignToExistingEnrollees")));
            relatedTask = tasks.getFirst();
        }

        participantTaskService.update(relatedTask, auditInfo);

        return relatedTask;
    }

    private ParticipantTask findTask(PortalParticipantUser ppUser, StudyEnvironment studyEnv, SurveyFormatter formatter, Map<String, String> enrolleeMap, Integer repeatNum, ZoneId zoneId) {
        ParticipantTask relatedTask = null;

        Optional<Instant> completedAt = parseSurveyFieldToInstant(formatter, enrolleeMap, repeatNum, "completedAt", zoneId);

        // attempt to find existing task to update
        if (repeatNum == 1) {
            // case 1; it's first repeat and no completedAt specified,
            // just grab latest
            relatedTask = participantTaskService.findTaskForActivity(ppUser.getId(), studyEnv.getId(), formatter.getModuleName())
                    .orElse(null);
        } else {
            if (completedAt.isEmpty()) {
                throw new IllegalStateException("Failed importing %s: completedAt must be specified for importing survey response history".formatted(formatter.getModuleName()));
            }
            // case 2: completedAt is specified, find task with that completion time
            relatedTask = participantTaskService.findTaskForActivityWithCompletionTime(ppUser.getId(), studyEnv.getId(), formatter.getModuleName(), completedAt.get())
                    .orElse(null);
        }

        return relatedTask;
    }


    // preserving response creation and task creation/completion
    // is important for recurring surveys
    private void shiftTime(SurveyResponse surveyResponse, ParticipantTask relatedTask, SurveyFormatter formatter, Map<String, String> enrolleeMap, Integer repeatNum, ZoneId zoneId) {
        // make sure the task reflects created and completion status
        // so that recurrences are properly scheduled
        Optional<Instant> completedAtOpt = parseSurveyFieldToInstant(formatter, enrolleeMap, repeatNum, "completedAt", zoneId);
        Optional<Instant> createdAtOpt = parseSurveyFieldToInstant(formatter, enrolleeMap, repeatNum, "createdAt", zoneId);
        Optional<Instant> lastUpdatedAtOpt = parseSurveyFieldToInstant(formatter, enrolleeMap, repeatNum, "lastUpdatedAt", zoneId);

        // use any of the three times if available
        Instant completedAt = completedAtOpt.orElseGet(() -> createdAtOpt.orElseGet(() -> lastUpdatedAtOpt.orElse(null)));
        Instant createdAt = createdAtOpt.orElseGet(() -> completedAtOpt.orElseGet(() -> lastUpdatedAtOpt.orElse(null)));
        Instant lastUpdatedAt = lastUpdatedAtOpt.orElseGet(() -> completedAtOpt.orElseGet(() -> createdAtOpt.orElse(null)));

        if (completedAt != null) {
            timeShiftDao.changeTaskCompleteTime(relatedTask.getId(), completedAt);
        }
        if (createdAt != null) {
            timeShiftDao.changeTasksCreationTime(List.of(relatedTask.getId()), createdAt);
            timeShiftDao.changeSurveyResponseCreationTime(surveyResponse.getId(), createdAt);
        }
        if (lastUpdatedAt != null) {
            timeShiftDao.changeSurveyResponseLastUpdatedTime(surveyResponse.getId(), lastUpdatedAt);
        }
    }

    private Optional<Instant> parseSurveyFieldToInstant(SurveyFormatter formatter, Map<String, String> enrolleeMap, Integer repeatNum, String field, ZoneId zoneId) {
        String key = formatter.formatColumnKey(repeatNum, field);
        if (enrolleeMap.containsKey(key)) {
            return Optional.ofNullable(ExportFormatUtils.importInstant(enrolleeMap.get(key), zoneId == null ? ZoneId.of("America/New_York") : zoneId));
        }
        return Optional.empty();
    }

    public static void copyNonNullProperties(Object source, Object target, List<String> ignoreProperties) {
        String[] ignorePropertiesArray = Stream.of(ignoreProperties, getNullPropertyNames(source)).flatMap(Collection::stream).toArray(String[]::new);
        BeanUtils.copyProperties(source, target, ignorePropertiesArray);
    }


    public static List<String> getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toList();
    }
}
