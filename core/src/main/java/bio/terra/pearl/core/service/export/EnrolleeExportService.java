package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.export.formatters.module.*;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
public class EnrolleeExportService {
    private final ProfileService profileService;
    private final AnswerDao answerDao;
    private final SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final SurveyResponseService surveyResponseService;
    private final ParticipantTaskService participantTaskService;
    private final EnrolleeService enrolleeService;
    private final KitRequestService kitRequestService;
    private final ParticipantUserService participantUserService;
    private final KitTypeDao kitTypeDao;
    private final ObjectMapper objectMapper;

    public EnrolleeExportService(ProfileService profileService,
                                 AnswerDao answerDao,
                                 SurveyQuestionDefinitionDao surveyQuestionDefinitionDao,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService, SurveyResponseService surveyResponseService,
                                 ParticipantTaskService participantTaskService,
                                 EnrolleeService enrolleeService, KitRequestService kitRequestService,
                                 ParticipantUserService participantUserService,
                                 KitTypeDao kitTypeDao, ObjectMapper objectMapper) {
        this.profileService = profileService;
        this.answerDao = answerDao;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
        this.kitRequestService = kitRequestService;
        this.participantUserService = participantUserService;
        this.kitTypeDao = kitTypeDao;
        this.objectMapper = objectMapper;
    }

    /**
     * exports the specified number of enrollees from the given environment
     * The enrollees will be returned most-recently-created first
     * */
    public void export(ExportOptions exportOptions, UUID studyEnvironmentId, OutputStream os) {
        List<ModuleFormatter> moduleFormatters = generateModuleInfos(exportOptions, studyEnvironmentId);
        List<Map<String, String>> enrolleeMaps = generateExportMaps(studyEnvironmentId, moduleFormatters, exportOptions.limit());
        BaseExporter exporter = getExporter(exportOptions.fileFormat(), moduleFormatters, enrolleeMaps);
        exporter.export(os);
    }

    public List<Map<String, String>> generateExportMaps(UUID studyEnvironmentId,
                                                   List<ModuleFormatter> moduleFormatters, Integer limit) {
        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(studyEnvironmentId, true, "created_at", "DESC");
        if (limit != null && enrollees.size() > 0) {
            enrollees = enrollees.subList(0, Math.min(enrollees.size(), limit));
        }
        return generateExportMaps(enrollees, moduleFormatters);
    }

    public List<Map<String, String>> generateExportMaps(List<Enrollee> enrollees,
                                                        List<ModuleFormatter> moduleFormatters) {
        List<EnrolleeExportData> enrolleeExportData = loadAllEnrolleesForExport(enrollees);

        List<Map<String, String>> exportMaps = new ArrayList<>();
        for (EnrolleeExportData exportData : enrolleeExportData) {
            exportMaps.add(generateExportMap(exportData, moduleFormatters));
        }
        return exportMaps;
    }

    public Map<String, String> generateExportMap(EnrolleeExportData exportData,
                                                 List<ModuleFormatter> moduleFormatters) {
        Map<String, String> valueMap = new HashMap<>();
        for (ModuleFormatter moduleExportInfo : moduleFormatters) {
            valueMap.putAll(moduleExportInfo.toStringMap(exportData));
        }
        return valueMap;
    }

    /**
     * gets information about the modules, which will determine the columns needed for the export
     * e.g. the columns needed to represent the survey questions.
     */
    public List<ModuleFormatter> generateModuleInfos(ExportOptions exportOptions, UUID studyEnvironmentId)  {
        List<ModuleFormatter> moduleFormatters = new ArrayList<>();
        moduleFormatters.add(new EnrolleeFormatter(exportOptions));
        moduleFormatters.add(new ParticipantUserFormatter(exportOptions));
        moduleFormatters.add(new ProfileFormatter(exportOptions));
        moduleFormatters.add(new KitRequestFormatter());
        moduleFormatters.addAll(generateSurveyModules(exportOptions, studyEnvironmentId));
        return moduleFormatters;
    }

    List<SurveyType> SURVEY_TYPE_EXPORT_ORDER = List.of(SurveyType.CONSENT, SurveyType.RESEARCH, SurveyType.OUTREACH);

    /**
     * returns a ModuleExportInfo for each unique survey stableId that has ever been attached to the studyEnvironment
     * If multiple versions of a survey have been attached, those will be consolidated into a single ModuleExportInfo
     */
    protected List<SurveyFormatter> generateSurveyModules(ExportOptions exportOptions, UUID studyEnvironmentId) {
        // get all surveys that have ever been attached to the StudyEnvironment, including inactive ones
        List<StudyEnvironmentSurvey> configuredSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurvey(studyEnvironmentId, null);
        Map<String, List<StudyEnvironmentSurvey>> configuredSurveysByStableId = configuredSurveys.stream().collect(
                groupingBy(cfgSurvey -> cfgSurvey.getSurvey().getStableId())
        );

        // sort by surveyType, then by surveyOrder so the resulting moduleExportInfo list is in the same order that participants take them
        List<Map.Entry<String, List<StudyEnvironmentSurvey>>> sortedCfgSurveysByStableId = configuredSurveysByStableId.entrySet()
                .stream().sorted(Comparator.comparing(entry ->
                        SURVEY_TYPE_EXPORT_ORDER.indexOf(((Map.Entry<String, List<StudyEnvironmentSurvey>>) entry).getValue().get(0).getSurvey().getSurveyType()))
                        .thenComparing(entry -> ((Map.Entry<String, List<StudyEnvironmentSurvey>>) entry).getValue().get(0).getSurveyOrder()))
                .toList();

        // create one moduleExportInfo for each survey stableId.
        List<SurveyFormatter> moduleFormatters = new ArrayList<>();
        for (Map.Entry<String, List<StudyEnvironmentSurvey>> surveysOfStableId : sortedCfgSurveysByStableId) {
            List<Survey> surveys = surveysOfStableId.getValue().stream().map(StudyEnvironmentSurvey::getSurvey).toList();
            List<SurveyQuestionDefinition> surveyQuestionDefinitions = surveyQuestionDefinitionDao.findAllBySurveyIds(surveys.stream().map(Survey::getId).toList());
            moduleFormatters.add(new SurveyFormatter(exportOptions, surveysOfStableId.getKey(), surveys, surveyQuestionDefinitions, objectMapper));
        }

        return moduleFormatters;
    }

    protected List<EnrolleeExportData> loadAllEnrolleesForExport(List<Enrollee> enrollees) {
        // for now, load each enrollee individually.  Later we'll want more sophisticated batching strategies
        return enrollees.stream().map(enrollee ->
                loadEnrolleeData(enrollee)
        ).toList();
    }

    protected EnrolleeExportData loadEnrolleeData(Enrollee enrollee) {
        return new EnrolleeExportData(
                enrollee,
                participantUserService.find(enrollee.getParticipantUserId()).orElseThrow(),
                profileService.loadWithMailingAddress(enrollee.getProfileId()).get(),
                answerDao.findByEnrolleeId(enrollee.getId()),
                participantTaskService.findByEnrolleeId(enrollee.getId()),
                surveyResponseService.findByEnrolleeId(enrollee.getId()),
                kitRequestService.findByEnrollee(enrollee)
        );
    }

    protected BaseExporter getExporter(ExportFileFormat fileFormat, List<ModuleFormatter> moduleFormatters,
                                       List<Map<String, String>> enrolleeMaps) {
        if (fileFormat.equals(ExportFileFormat.JSON)) {
            return new JsonExporter(moduleFormatters, enrolleeMaps, objectMapper);
        } else if (fileFormat.equals(ExportFileFormat.EXCEL)) {
            return new ExcelExporter(moduleFormatters, enrolleeMaps);
        }
        return new TsvExporter(moduleFormatters, enrolleeMaps);
    }


}
