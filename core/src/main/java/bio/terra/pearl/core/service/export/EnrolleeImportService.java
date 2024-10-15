package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.dao.dataimport.TimeShiftPopulateDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.dataimport.*;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.export.dataimport.ImportFileFormat;
import bio.terra.pearl.core.service.export.dataimport.ImportItemService;
import bio.terra.pearl.core.service.export.dataimport.ImportService;
import bio.terra.pearl.core.service.export.formatters.module.*;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.*;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class EnrolleeImportService {

    private final EnrolleeRelationService enrolleeRelationService;
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
    private final TimeShiftPopulateDao timeShiftPopulateDao;
    private final ImportService importService;
    private final ImportItemService importItemService;
    private final KitRequestService kitRequestService;
    private final char CSV_DELIMITER = ',';
    private final char TSV_DELIMITER = '\t';

    public EnrolleeImportService(RegistrationService registrationService, EnrollmentService enrollmentService,
                                 ProfileService profileService, EnrolleeExportService enrolleeExportService,
                                 SurveyResponseService surveyResponseService, ParticipantTaskService participantTaskService, PortalService portalService,
                                 ImportService importService, ImportItemService importItemService, SurveyTaskDispatcher surveyTaskDispatcher,
                                 TimeShiftPopulateDao timeShiftPopulateDao, EnrolleeService enrolleeService, ParticipantUserService participantUserService,
                                 PortalParticipantUserService portalParticipantUserService, KitRequestService kitRequestService, EnrolleeRelationService enrolleeRelationService) {
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
        this.timeShiftPopulateDao = timeShiftPopulateDao;
        this.enrolleeService = enrolleeService;
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.kitRequestService = kitRequestService;
        this.enrolleeRelationService = enrolleeRelationService;
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
            if (email == null || email.isBlank()) {
                email = proxyEmail;
            }

            AccountImportData existingAccount = findExistingAccount(accountData, email);
            if (proxyEmail == null || proxyEmail.isBlank()) {
                if (existingAccount == null) {
                    AccountImportData newAccount = new AccountImportData();
                    newAccount.setEmail(email);
                    newAccount.setEnrolleeData(enrolleeMap);
                    accountData.add(newAccount);
                } else {
                    existingAccount.setEnrolleeData(enrolleeMap);
                }
            } else {
                if (existingAccount == null) {
                    AccountImportData newAccount = new AccountImportData();
                    newAccount.setEmail(email);
                    newAccount.getProxyData().add(enrolleeMap);
                    accountData.add(newAccount);
                } else {
                    existingAccount.getProxyData().add(enrolleeMap);
                }
            }
        }

        return accountData;
    }

    AccountImportData findExistingAccount(List<AccountImportData> accountData, String email) {
        for (AccountImportData account : accountData) {
            if (account.getEmail().equals(email)) {
                return account;
            }
        }
        return null;
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
            importItems.add(createFailedImportItem(importId, e.getMessage(), Arrays.toString(e.getStackTrace()), adminId));
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
        final ParticipantUser participantUserInfo = participantUserFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);

        final RegistrationService.RegistrationResult regResult = registerIfNeeded(portalShortcode, studyEnv, participantUserInfo);

        Enrollee enrollee = createEnrolleeIfNeeded(studyShortcode, studyEnv, enrolleeMap, exportOptions, regResult, auditInfo, participantUserInfo);

        /** now update the profile */
        Profile profile = importProfile(enrolleeMap, regResult.profile(), exportOptions, studyEnv, auditInfo);

        /** populate kit_requests */
        importKitRequests(enrolleeMap, adminId, enrollee);

        importSurveyResponses(portalShortcode, enrolleeMap, exportOptions, studyEnv, regResult.portalParticipantUser(), enrollee, auditInfo);

        /** restore email -- reload the profile since answermappings may have changed it */
        profile = profileService.find(profile.getId()).orElseThrow();
        profile.setDoNotEmail(false);
        profileService.update(profile, auditInfo);
        return enrollee;
    }

    private void importKitRequests(Map<String, String> enrolleeMap, UUID adminId, Enrollee enrollee) {
        new KitRequestFormatter().listFromStringMap(enrolleeMap).stream().map(
                kitRequestDto -> kitRequestService.create(convertKitRequestDto(adminId, enrollee, kitRequestDto))).toList();
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
            Enrollee enrolleeInfo = enrolleeFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);
            /** temporarily update the profile to no emails since they'll receive a special welcome email */
            regResult.profile().setDoNotEmail(true);
            profileService.update(regResult.profile(), auditInfo);

            HubResponse<Enrollee> response = enrollmentService.enroll(regResult.portalParticipantUser(), studyEnv.getEnvironmentName(),
                    studyShortcode, regResult.participantUser(), regResult.portalParticipantUser(), null, enrolleeInfo.isSubject());
            Enrollee newEnrollee = response.getEnrollee();
            //update createdAt
            if (newEnrollee.getCreatedAt() != null) {
                timeShiftPopulateDao.changeEnrolleeCreationTime(response.getEnrollee().getId(), enrolleeInfo.getCreatedAt());
            }
            if (regResult.participantUser().getCreatedAt() != null) {
                timeShiftPopulateDao.changeParticipantAccountCreationTime(response.getEnrollee().getParticipantUserId(), participantUserInfo.getCreatedAt());
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
        Profile importProfile = profileFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);
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
                                                         PortalParticipantUser ppUser,
                                                         Enrollee enrollee,
                                                         DataAuditInfo auditInfo) {
        List<SurveyFormatter> surveyModules = enrolleeExportService.generateSurveyModules(exportOptions, studyEnv.getId(), List.of());
        List<SurveyResponse> responses = new ArrayList<>();
        UUID portalId = portalService.findOneByShortcode(portalShortcode).orElseThrow().getId();
        for (SurveyFormatter formatter : surveyModules) {
            SurveyResponse surveyResponse = importSurveyResponse(portalId, formatter, enrolleeMap, exportOptions, studyEnv, ppUser, enrollee, auditInfo);
            if (surveyResponse != null) {
                responses.add(surveyResponse);
            }
        }
        return responses;
    }

    protected SurveyResponse importSurveyResponse(UUID portalId, SurveyFormatter formatter, Map<String, String> enrolleeMap, ExportOptions exportOptions,
                                                  StudyEnvironment studyEnv, PortalParticipantUser ppUser, Enrollee enrollee, DataAuditInfo auditInfo) {
        SurveyResponse response = formatter.fromStringMap(studyEnv.getId(), enrolleeMap);
        if (response == null) {
            return null;
        }
        ParticipantTask relatedTask = participantTaskService.findTaskForActivity(ppUser.getId(), studyEnv.getId(), formatter.getModuleName())
                .orElse(null);
        if (relatedTask == null) {
            ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(
                    TaskType.SURVEY,
                    formatter.getModuleName(),
                    1,
                    List.of(enrollee.getId()),
                    false,
                    true);

            List<ParticipantTask> tasks = surveyTaskDispatcher.assign(assignDto, studyEnv.getId(),
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleSurveyPublished.assignToExistingEnrollees")));
            relatedTask = tasks.getFirst();
        }
        // we're not worrying about dating the response yet
        return surveyResponseService.updateResponse(response, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "importSurveyResponse")),
                "Imported", ppUser, enrollee, relatedTask.getId(), portalId).getResponse();
    }

    public static void copyNonNullProperties(Object source, Object target, List<String> ignoreProperties) {
        String[] ignorePropertiesArray = Stream.of(ignoreProperties, getNullPropertyNames(source)).flatMap(Collection::stream).toArray(String[]::new);
        BeanUtils.copyProperties(source, target, ignorePropertiesArray);
    }


    public static List<String> getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(java.beans.FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toList();
    }
}
