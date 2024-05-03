package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItem;
import bio.terra.pearl.core.model.dataimport.ImportItemStatus;
import bio.terra.pearl.core.model.dataimport.ImportStatus;
import bio.terra.pearl.core.model.dataimport.ImportType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.dataimport.ImportItemService;
import bio.terra.pearl.core.service.dataimport.ImportService;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.export.formatters.module.EnrolleeFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ParticipantUserFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ProfileFormatter;
import bio.terra.pearl.core.service.export.formatters.module.SurveyFormatter;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class EnrolleeImportService {
    /**
     * for now, we only support importing from a specific style of export.
     */
    ExportOptions IMPORT_OPTIONS = ExportOptions
            .builder()
            .stableIdsForOptions(true)
            .onlyIncludeMostRecent(true)
            .fileFormat(ExportFileFormat.TSV)
            .limit(null)
            .build();

    private final RegistrationService registrationService;
    private final EnrollmentService enrollmentService;
    private final ProfileService profileService;
    private final EnrolleeExportService enrolleeExportService;
    private final SurveyResponseService surveyResponseService;
    private final ParticipantTaskService participantTaskService;
    private final PortalService portalService;
    private final ImportService importService;
    private final ImportItemService importItemService;

    public EnrolleeImportService(RegistrationService registrationService, EnrollmentService enrollmentService,
                                 ProfileService profileService, EnrolleeExportService enrolleeExportService,
                                 SurveyResponseService surveyResponseService, ParticipantTaskService participantTaskService, PortalService portalService,
                                 ImportService importService, ImportItemService importItemService) {
        this.registrationService = registrationService;
        this.enrollmentService = enrollmentService;
        this.profileService = profileService;
        this.enrolleeExportService = enrolleeExportService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.portalService = portalService;
        this.importService = importService;
        this.importItemService = importItemService;
    }

    @Transactional
    /**
     * imports the enrollees serialized in the inputstream to the given environment
     */
    public Import importEnrollees(String portalShortcode, String studyShortcode, StudyEnvironment studyEnv, InputStream in, UUID adminId) {
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
        List<Map<String, String>> enrolleeMaps = generateImportMaps(in);
        for (Map<String, String> enrolleeMap : enrolleeMaps) {
            Enrollee enrollee = null;
            ImportItem importItem;
            try {
                enrollee = importEnrollee(portalShortcode, studyShortcode, studyEnv, enrolleeMap, IMPORT_OPTIONS);
                importItem = ImportItem.builder()
                        .createdEnrolleeId(enrollee.getId())
                        .importId(dataImport.getId())
                        .createdParticipantUserId(enrollee.getParticipantUserId())
                        .createdAt(Instant.now())
                        .lastUpdatedAt(Instant.now())
                        .status(ImportItemStatus.SUCCESS).build();
            } catch (Exception e) {
                importItem = ImportItem.builder()
                        .importId(dataImport.getId())
                        .createdParticipantUserId(adminId)
                        .createdAt(Instant.now())
                        .lastUpdatedAt(Instant.now())
                        .status(ImportItemStatus.FAILED)
                        .message(e.getMessage())
                        .detail(Arrays.toString(e.getStackTrace())).build();
            }

            importItem = importItemService.create(importItem);
            log.debug("populated Import Item ID: {}", importItem.getId());
        }
        dataImport.setStatus(ImportStatus.DONE);
        importService.update(dataImport);
        importItemService.attachImportItems(dataImport);
        log.info("Completed importing : {} items for Import ID: {}", dataImport.getImportItems().size(), dataImport.getId());
        return dataImport;
    }

    /**
     * transforms a TSV import input stream into a List of string maps, one map per enrollee
     */
    public List<Map<String, String>> generateImportMaps(InputStream in) {
        List<Map<String, String>> importMaps = new ArrayList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator('\t').build();
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(in)).withCSVParser(parser).build()) {
            String[] headers = csvReader.readNext();
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                Map<String, String> enrolleeMap = new HashMap<>();
                for (int i = 0; i < line.length; i++) {
                    enrolleeMap.put(headers[i], line[i]);
                }
                importMaps.add(enrolleeMap);
            }
            return importMaps;
        } catch (IOException | CsvValidationException e) {
            throw new InternalServerException("error reading input stream", e);
        }
    }

    public Enrollee importEnrollee(String portalShortcode, String studyShortcode, StudyEnvironment studyEnv, Map<String, String> enrolleeMap, ExportOptions exportOptions) {

        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "importEnrollee")
        ).build();

        /** first create the participant user */
        ParticipantUserFormatter participantUserFormatter = new ParticipantUserFormatter(exportOptions);
        ParticipantUser participantUser = participantUserFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);
        if (participantUser.getUsername() == null) {
            throw new IllegalArgumentException("username must be provided for enrollee import");
        }
        RegistrationService.RegistrationResult regResult = registrationService.register(portalShortcode, studyEnv.getEnvironmentName(), participantUser.getUsername(), null, null);
        /** temporarily update the profile to no emails since they'll receive a special welcome email */
        regResult.profile().setDoNotEmail(true);
        profileService.update(regResult.profile(), auditInfo);

        /** now create the enrollee */
        EnrolleeFormatter enrolleeFormatter = new EnrolleeFormatter(exportOptions);
        Enrollee enrollee = enrolleeFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);
        HubResponse<Enrollee> response = enrollmentService.enroll(regResult.portalParticipantUser(), studyEnv.getEnvironmentName(), studyShortcode, regResult.participantUser(), regResult.portalParticipantUser(), null, enrollee.isSubject());

        /** now update the profile */
        Profile profile = importProfile(enrolleeMap, regResult.profile(), exportOptions, studyEnv, auditInfo);

        List<SurveyResponse> surveyResponses = importSurveyResponses(portalShortcode, enrolleeMap, exportOptions, studyEnv, regResult.portalParticipantUser(), response.getEnrollee(), auditInfo);

        /** restore email */
        profile.setDoNotEmail(false);
        profileService.update(profile, auditInfo);
        return response.getEnrollee();
    }

    protected Profile importProfile(Map<String, String> enrolleeMap, Profile registrationProfile,
                                    ExportOptions exportOptions, StudyEnvironment studyEnv, DataAuditInfo auditInfo) {
        ProfileFormatter profileFormatter = new ProfileFormatter(exportOptions);
        Profile profile = profileFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);
        profile.setId(registrationProfile.getId());
        profile.setMailingAddressId(registrationProfile.getMailingAddressId());
        profile.getMailingAddress().setId(registrationProfile.getMailingAddressId());
        return profileService.updateWithMailingAddress(profile, auditInfo);
    }

    protected List<SurveyResponse> importSurveyResponses(String portalShortcode,
                                                         Map<String, String> enrolleeMap,
                                                         ExportOptions exportOptions,
                                                         StudyEnvironment studyEnv,
                                                         PortalParticipantUser ppUser,
                                                         Enrollee enrollee,
                                                         DataAuditInfo auditInfo) {
        List<SurveyFormatter> surveyModules = enrolleeExportService.generateSurveyModules(exportOptions, studyEnv.getId());
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
        if (response != null) {
            ParticipantTask relatedTask = participantTaskService.findTaskForActivity(ppUser.getId(), studyEnv.getId(), formatter.getModuleName())
                    .orElseThrow(() -> new IllegalStateException("Task not found to enable import of response for " + formatter.getModuleName()));
            // we're not worrying about dating the response yet
            return surveyResponseService.updateResponse(response, enrollee.getParticipantUserId(), ppUser, enrollee, relatedTask.getId(), portalId).getResponse();
        } else {
            return null;
        }
    }
}
