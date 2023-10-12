package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.export.formatters.EnrolleeFormatter;
import bio.terra.pearl.core.service.export.formatters.ProfileFormatter;
import bio.terra.pearl.core.service.export.formatters.SurveyFormatter;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import bio.terra.pearl.core.service.participant.EnrolleeService;
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
    private final SurveyDao surveyDao;
    private final EnrolleeService enrolleeService;
    private final ObjectMapper objectMapper;

    public EnrolleeExportService(ProfileService profileService,
                                 AnswerDao answerDao,
                                 SurveyQuestionDefinitionDao surveyQuestionDefinitionDao,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService, SurveyResponseService surveyResponseService,
                                 ParticipantTaskService participantTaskService,
                                 SurveyDao surveyDao,
                                 EnrolleeService enrolleeService, ObjectMapper objectMapper) {
        this.profileService = profileService;
        this.answerDao = answerDao;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.surveyDao = surveyDao;
        this.enrolleeService = enrolleeService;
        this.objectMapper = objectMapper;
    }

    /**
     * exports the specified number of enrollees from the given environment
     * The enrollees will be returned most-recently-created first
     * */
    public void export(ExportOptions exportOptions, UUID studyEnvironmentId, OutputStream os) throws Exception {
        List<ModuleExportInfo> moduleExportInfos = generateModuleInfos(exportOptions, studyEnvironmentId);
        var enrolleeMaps = generateExportMaps(studyEnvironmentId,
                moduleExportInfos, exportOptions.limit());
        BaseExporter exporter = getExporter(exportOptions.fileFormat(), moduleExportInfos, enrolleeMaps);
        exporter.export(os);
    }

    public List<Map<String, String>> generateExportMaps(UUID studyEnvironmentId,
                                                   List<ModuleExportInfo> moduleExportInfos, Integer limit) throws Exception {
        List<Enrollee> enrollees = enrolleeService.findByStudyEnvironment(studyEnvironmentId, "created_at", "DESC");
        if (limit != null && enrollees.size() > 0) {
            enrollees = enrollees.subList(0, Math.min(enrollees.size(), limit));
        }
        return generateExportMaps(enrollees, moduleExportInfos);
    }

    public List<Map<String, String>> generateExportMaps(List<Enrollee> enrollees,
                                                        List<ModuleExportInfo> moduleExportInfos) throws Exception {
        List<EnrolleeExportData> enrolleeExportData = loadAllEnrolleesForExport(enrollees);

        List<Map<String, String>> exportMaps = new ArrayList<>();
        for (EnrolleeExportData exportData : enrolleeExportData) {
            exportMaps.add(generateExportMap(exportData, moduleExportInfos));
        }
        return exportMaps;
    }

    public Map<String, String> generateExportMap(EnrolleeExportData exportData,
                                                 List<ModuleExportInfo> moduleExportInfos) throws Exception {
        Map<String, String> valueMap = new HashMap<>();
        for (ModuleExportInfo moduleExportInfo : moduleExportInfos) {
            valueMap.putAll(moduleExportInfo.toStringMap(exportData));
        }
        return valueMap;
    }

    /**
     * gets information about the modules, which will determine the columns needed for the export
     * e.g. the columns needed to represent the survey questions.
     */
    public List<ModuleExportInfo> generateModuleInfos(ExportOptions exportOptions, UUID studyEnvironmentId) throws Exception {
        List<ModuleExportInfo> moduleInfo = new ArrayList<>();
        moduleInfo.add(new EnrolleeFormatter().getModuleExportInfo(exportOptions));
        moduleInfo.add(new ProfileFormatter().getModuleExportInfo(exportOptions));
        moduleInfo.addAll(generateSurveyModules(exportOptions, studyEnvironmentId));
        return moduleInfo;
    }

    protected List<ModuleExportInfo> generateSurveyModules(ExportOptions exportOptions, UUID studyEnvironmentId) throws Exception {
        // get all surveys that have ever been attached, including inactive
        List<StudyEnvironmentSurvey> configuredSurveys = studyEnvironmentSurveyService.findAllByStudyEnvIdWithSurvey(studyEnvironmentId, null);
        configuredSurveys.sort(Comparator.comparing(StudyEnvironmentSurvey::getSurveyOrder));
        SurveyFormatter surveyFormatter = new SurveyFormatter(objectMapper);
        List<ModuleExportInfo> moduleExportInfos = new ArrayList<>();
        Map<String, List<StudyEnvironmentSurvey>> surveysByStableId = configuredSurveys.stream().collect(
                groupingBy(cfgSurvey -> cfgSurvey.getSurvey().getStableId())
        );

        // create one moduleExportInfo for each survey stableId.
        for (Map.Entry<String, List<StudyEnvironmentSurvey>> surveysOfStableId : surveysByStableId.entrySet()) {
            List<Survey> surveys = surveysOfStableId.getValue().stream().map(StudyEnvironmentSurvey::getSurvey).toList();
            var surveyQuestionDefinitions = surveyQuestionDefinitionDao.findAllBySurveyIds(surveys.stream().map(Survey::getId).toList());
            moduleExportInfos.add(surveyFormatter.getModuleExportInfo(exportOptions, surveysOfStableId.getKey(), surveys, surveyQuestionDefinitions));
        }
        return moduleExportInfos;
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
                profileService.loadWithMailingAddress(enrollee.getProfileId()).get(),
                answerDao.findByEnrolleeId(enrollee.getId()),
                participantTaskService.findByEnrolleeId(enrollee.getId()),
                surveyResponseService.findByEnrolleeId(enrollee.getId())
        );
    }

    protected BaseExporter getExporter(ExportFileFormat fileFormat, List<ModuleExportInfo> moduleExportInfos,
                                       List<Map<String, String>> enrolleeMaps) {
        if (fileFormat.equals(ExportFileFormat.JSON)) {
            return new JsonExporter(moduleExportInfos, enrolleeMaps, objectMapper);
        } else if (fileFormat.equals(ExportFileFormat.EXCEL)) {
            return new ExcelExporter(moduleExportInfos, enrolleeMaps);
        }
        return new TsvExporter(moduleExportInfos, enrolleeMaps);
    }


}
