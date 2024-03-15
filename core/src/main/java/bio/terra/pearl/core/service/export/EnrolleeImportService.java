package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvWithShortcode;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.export.formatters.module.EnrolleeFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ParticipantUserFormatter;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class EnrolleeImportService {
    /** for now, we only support importing from a specific style of export. */
    ExportOptions IMPORT_OPTIONS = new ExportOptions(false, true, true, ExportFileFormat.TSV, null);

    private final RegistrationService registrationService;
    private final EnrollmentService enrollmentService;
    private final ProfileService profileService;

    public EnrolleeImportService(RegistrationService registrationService, EnrollmentService enrollmentService, ProfileService profileService) {
        this.registrationService = registrationService;
        this.enrollmentService = enrollmentService;
        this.profileService = profileService;
    }

    @Transactional
    /**
     * exports the specified number of enrollees from the given environment
     * The enrollees will be returned most-recently-created first
     * */
    public void importEnrollees(String portalShortcode, String studyShortcode, StudyEnvironment studyEnv, InputStream in) {
        List<Map<String, String>> enrolleeMaps = generateImportMaps(in);
        for (Map<String, String> enrolleeMap : enrolleeMaps) {
            Enrollee enrollee = importEnrollee(portalShortcode, studyShortcode, studyEnv, enrolleeMap, IMPORT_OPTIONS);
        }
    }

    /** transforms a TSV import input stream into a List of string maps, one map per enrollee */
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
        RegistrationService.RegistrationResult regResult = registrationService.register(portalShortcode, studyEnv.getEnvironmentName(), participantUser.getUsername(), null);
        /** temporarily update the profile to no emails since they'll receive a special welcome email */
        regResult.profile().setDoNotEmail(true);
        Profile profile = profileService.update(regResult.profile(), auditInfo);

        /** now create the enrollee */
        EnrolleeFormatter enrolleeFormatter = new EnrolleeFormatter(exportOptions);
        Enrollee enrollee = enrolleeFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);
        HubResponse<Enrollee> response = enrollmentService.enroll(studyEnv.getEnvironmentName(), studyShortcode, regResult.participantUser(), regResult.portalParticipantUser(), null, enrollee.isSubject());

        /** restore email */
        profile.setDoNotEmail(false);
        profileService.update(regResult.profile(), auditInfo);
        return response.getEnrollee();
    }

}
