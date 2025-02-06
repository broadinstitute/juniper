package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.dao.search.EnrolleeSearchExpressionDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.export.formatters.module.*;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.FamilyService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchOptions;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
public class EnrolleeExportService {
    private final ProfileService profileService;
    private final AnswerDao answerDao;
    private final SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final SurveyService surveyService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final SurveyResponseService surveyResponseService;
    private final ParticipantTaskService participantTaskService;
    private final KitRequestService kitRequestService;
    private final ParticipantUserService participantUserService;
    private final ObjectMapper objectMapper;
    private final EnrolleeRelationService enrolleeRelationService;
    private final FamilyService familyService;
    private final EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;
    private final StudyEnvironmentConfigService studyEnvironmentConfigService;
    private final StudyService studyService;

    public EnrolleeExportService(ProfileService profileService,
                                 AnswerDao answerDao,
                                 SurveyQuestionDefinitionDao surveyQuestionDefinitionDao,
                                 StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                 SurveyService surveyService, StudyEnvironmentService studyEnvironmentService, SurveyResponseService surveyResponseService,
                                 ParticipantTaskService participantTaskService,
                                 KitRequestService kitRequestService,
                                 ParticipantUserService participantUserService,
                                 EnrolleeRelationService enrolleeRelationService,
                                 ObjectMapper objectMapper,
                                 FamilyService familyService,
                                 EnrolleeSearchExpressionDao enrolleeSearchExpressionDao,
                                 StudyEnvironmentConfigService studyEnvironmentConfigService,
                                 StudyService studyService) {
        this.profileService = profileService;
        this.answerDao = answerDao;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.surveyService = surveyService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.kitRequestService = kitRequestService;
        this.participantUserService = participantUserService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.objectMapper = objectMapper;
        this.familyService = familyService;
        this.enrolleeSearchExpressionDao = enrolleeSearchExpressionDao;
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.studyService = studyService;
    }

    /**
     * exports the specified number of enrollees from the given environment
     * The enrollees will be returned most-recently-created first
     * */
    public void export(ExportOptionsWithExpression exportOptions, UUID studyEnvironmentId, OutputStream os) {

        List<EnrolleeExportData> enrolleeExportData = loadEnrolleeExportData(studyEnvironmentId, exportOptions);

        List<ModuleFormatter> moduleFormatters = generateModuleInfos(exportOptions, studyEnvironmentId, enrolleeExportData);
        List<Map<String, String>> enrolleeMaps = generateExportMaps(enrolleeExportData, moduleFormatters);
        BaseExporter exporter = getExporter(exportOptions.getFileFormat(), moduleFormatters, enrolleeMaps, exportOptions.getIncludeFields());
        exporter.export(os, exportOptions.isIncludeSubHeaders());
    }

    private List<Enrollee> loadEnrollees(UUID studyEnvironmentId, EnrolleeSearchExpression filter, Integer limit) {
        List<EnrolleeSearchExpressionResult> results =
                enrolleeSearchExpressionDao.executeSearch(
                        filter,
                        studyEnvironmentId,
                        EnrolleeSearchOptions.builder().sortField("enrollee.created_at").sortAscending(false).build());

        if (limit != null && !results.isEmpty()) {
            results = results.subList(0, Math.min(results.size(), limit));
        }

        return results.stream()
                .map(EnrolleeSearchExpressionResult::getEnrollee)
                .toList();

    }

    public List<Map<String, String>> generateExportMaps(List<EnrolleeExportData> enrolleeExportData,
                                                        List<ModuleFormatter> moduleFormatters) {

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
    public List<ModuleFormatter> generateModuleInfos(ExportOptions exportOptions, UUID studyEnvironmentId, List<EnrolleeExportData> enrolleeExportData) {
        List<ModuleFormatter> allSimpleFormatters = List.of(
                new EnrolleeFormatter(exportOptions),
                new StudyFormatter(exportOptions),
                new ParticipantUserFormatter(exportOptions),
                new ProfileFormatter(exportOptions),
                new KitRequestFormatter(exportOptions),
                new EnrolleeRelationFormatter(exportOptions),
                new FamilyFormatter(exportOptions),
                new ProxyFormatter(exportOptions));

        List<ModuleFormatter> moduleFormatters = allSimpleFormatters.stream().filter(
                moduleFormatter -> !exportOptions.getExcludeModules().contains(moduleFormatter.getModuleName())
        ).collect(Collectors.toList());
        if (!exportOptions.getExcludeModules().contains("surveys")) {
            moduleFormatters.addAll(generateSurveyModules(exportOptions, studyEnvironmentId, enrolleeExportData));
        }
        return moduleFormatters;
    }

    List<SurveyType> SURVEY_TYPE_EXPORT_ORDER = List.of(SurveyType.PRE_ENROLL, SurveyType.CONSENT, SurveyType.RESEARCH, SurveyType.DOCUMENT_REQUEST, SurveyType.OUTREACH);

    /**
     * returns a ModuleExportInfo for each unique survey stableId that has ever been attached to the studyEnvironment
     * If multiple versions of a survey have been attached, those will be consolidated into a single ModuleExportInfo
     */
    protected List<SurveyFormatter> generateSurveyModules(ExportOptions exportOptions, UUID studyEnvironmentId, List<EnrolleeExportData> enrolleeExportData) {
        List<SurveyFormatter> moduleFormatters = new ArrayList<>();

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
        for (Map.Entry<String, List<StudyEnvironmentSurvey>> surveysOfStableId : sortedCfgSurveysByStableId) {
            List<Survey> surveys = surveysOfStableId.getValue().stream().map(StudyEnvironmentSurvey::getSurvey).toList();
            List<SurveyQuestionDefinition> surveyQuestionDefinitions = surveyQuestionDefinitionDao.findAllBySurveyIds(surveys.stream().map(Survey::getId).toList());
            moduleFormatters.add(new SurveyFormatter(
                    exportOptions,
                    surveysOfStableId.getKey(),
                    surveys,
                    surveyQuestionDefinitions,
                    enrolleeExportData,
                    objectMapper));
        }
        return moduleFormatters;
    }

    public List<EnrolleeExportData> loadEnrolleeExportData(UUID studyEnvironmentId, ExportOptionsWithExpression exportOptions) {
        Study study = studyService.findByStudyEnvironmentId(studyEnvironmentId).orElseThrow();
        List<Enrollee> enrollees = loadEnrollees(studyEnvironmentId, exportOptions.getFilterExpression(), exportOptions.getRowLimit());

        List<UUID> enrolleeIds = enrollees.stream().map(Enrollee::getId).toList();
        List<UUID> profileIds = enrollees.stream().map(Enrollee::getProfileId).toList();
        List<UUID> participantUserIds = enrollees.stream().map(Enrollee::getParticipantUserId).toList();

        // batch load the following modules to reduce the number of queries and reduce the memory footprint of data exports.
        // eventually, the in-clauses of these sql queries will be too large, and we'll need to batch load these in smaller chunks
        Map<UUID, Profile> profiles = profileService.loadAllWithMailingAddress(profileIds);
        Map<UUID, ParticipantUser> participantUsers = participantUserService.findByParticipantUserIds(participantUserIds);
        Map<UUID, List<Answer>> answers = answerDao.findByEnrolleeIds(enrolleeIds);
        Map<UUID, List<ParticipantTask>> tasks = participantTaskService.findByEnrolleeIds(enrolleeIds);
        Map<UUID, List<SurveyResponseWithTaskDto>> allSurveyResponses =
                attachTasksToSurveyResponses(
                        tasks,
                        surveyResponseService.findByEnrolleeIdsNotRemoved(
                                enrolleeIds));
        Map<UUID, List<SurveyResponseWithTaskDto>> surveyResponses = exportOptions.isOnlyIncludeMostRecent()
                ? filterToMostRecentSurveyResponses(allSurveyResponses)
                : allSurveyResponses;

        Map<UUID, List<KitRequestDto>> kitRequests = kitRequestService.findByEnrollees(enrollees);

        return enrollees.stream()
                .map(enrollee -> loadEnrolleeData(study, studyEnvironmentConfigService.findByStudyEnvironmentId(studyEnvironmentId), enrollee, profiles, participantUsers, answers, tasks, surveyResponses, kitRequests))
                .toList();
    }

    protected EnrolleeExportData loadEnrolleeData(Study study, StudyEnvironmentConfig config, Enrollee enrollee,
                                                  Map<UUID, Profile> profiles, Map<UUID, ParticipantUser> participantUsers,
                                                  Map<UUID, List<Answer>> answers, Map<UUID, List<ParticipantTask>> tasks,
                                                  Map<UUID, List<SurveyResponseWithTaskDto>> surveyResponses, Map<UUID, List<KitRequestDto>> kitRequests) {

        List<EnrolleeRelation> enrolleeRelations = loadRelations(config, enrollee);
        List<ParticipantUser> proxies = loadProxyUsers(config, enrolleeRelations);

        return new EnrolleeExportData(
                study,
                enrollee,
                participantUsers.get(enrollee.getParticipantUserId()),
                profiles.get(enrollee.getProfileId()),
                answers.getOrDefault(enrollee.getId(), Collections.emptyList()),
                tasks.getOrDefault(enrollee.getId(), Collections.emptyList()),
                surveyResponses
                        .getOrDefault(enrollee.getId(), Collections.emptyList())
                        .stream()
                        .sorted(Comparator.comparing(SurveyResponse::getCreatedAt).reversed())
                        .toList(),
                kitRequests.getOrDefault(enrollee.getId(), Collections.emptyList()),
                enrolleeRelations,
                config.isEnableFamilyLinkage() ? familyService.findByEnrolleeIdWithProband(enrollee.getId()) : Collections.emptyList(),
                proxies
        );
    }

    private List<ParticipantUser> loadProxyUsers(StudyEnvironmentConfig config, List<EnrolleeRelation> relations) {
        if (config.isAcceptingProxyEnrollment()) {
            return relations.stream()
                    .filter(relation -> relation.getRelationshipType().equals(RelationshipType.PROXY))
                    .map(relation -> participantUserService.findByEnrolleeId(relation.getEnrolleeId()).orElseThrow())
                    .toList();
        }

        return Collections.emptyList();
    }

    private List<EnrolleeRelation> loadRelations(StudyEnvironmentConfig config, Enrollee enrollee) {
        if (config.isEnableFamilyLinkage()) {
            return enrolleeRelationService.findByTargetEnrolleeIdWithEnrolleesAndFamily(enrollee.getId());
        }
        if (config.isAcceptingProxyEnrollment()) {
            return enrolleeRelationService.findByTargetEnrolleeIdWithEnrollees(enrollee.getId());
        }

        // for performance reasons, we should grab nothing unless the study environment is configured to use
        // family linkage or proxy enrollment
        return Collections.emptyList();
    }

    protected BaseExporter getExporter(ExportFileFormat fileFormat, List<ModuleFormatter> moduleFormatters,
                                       List<Map<String, String>> enrolleeMaps, List<String> columnSorting) {
        if (fileFormat.equals(ExportFileFormat.JSON)) {
            return new JsonExporter(moduleFormatters, enrolleeMaps, columnSorting, objectMapper);
        } else if (fileFormat.equals(ExportFileFormat.EXCEL)) {
            return new ExcelExporter(moduleFormatters, enrolleeMaps, columnSorting);
        }
        return new TsvExporter(moduleFormatters, enrolleeMaps, fileFormat, columnSorting);
    }

    private Map<UUID, List<SurveyResponseWithTaskDto>> attachTasksToSurveyResponses(Map<UUID, List<ParticipantTask>> taskMap, Map<UUID, List<SurveyResponse>> surveyResponseMap) {
        Map<UUID, List<SurveyResponseWithTaskDto>> surveyResponseWithTaskMap = new HashMap<>();

        for (UUID enrolleeId : surveyResponseMap.keySet()) {
            List<ParticipantTask> tasks = taskMap.getOrDefault(enrolleeId, new ArrayList<>());
            List<SurveyResponse> surveyResponses = surveyResponseMap.getOrDefault(enrolleeId, new ArrayList<>());
            List<SurveyResponseWithTaskDto> surveyResponseWithTaskDtos = new ArrayList<>();

            for (SurveyResponse surveyResponse : surveyResponses) {
                ParticipantTask taskForSurveyResponse = tasks.stream()
                        .filter(task -> surveyResponse.getId().equals(task.getSurveyResponseId()))
                        .sorted(Comparator.comparing(ParticipantTask::getCreatedAt).reversed())
                        .findAny()
                        .orElse(null);

                surveyResponseWithTaskDtos.add(
                        new SurveyResponseWithTaskDto(
                                surveyResponse,
                                taskForSurveyResponse
                        )
                );
            }

            surveyResponseWithTaskMap.put(enrolleeId, surveyResponseWithTaskDtos);
        }

        return surveyResponseWithTaskMap;
    }

    private Map<UUID, List<SurveyResponseWithTaskDto>> filterToMostRecentSurveyResponses(Map<UUID, List<SurveyResponseWithTaskDto>> surveyResponseWithTaskMap) {
        return surveyResponseWithTaskMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> filterToMostRecentSurveyResponses(entry.getValue())
                ));
    }

    private List<SurveyResponseWithTaskDto> filterToMostRecentSurveyResponses(List<SurveyResponseWithTaskDto> surveyResponses) {
        return surveyResponses
                .stream()
                .collect(Collectors.groupingBy(sr -> sr.getTask().getTargetStableId()))
                .values()
                .stream()
                .map(surveyResponsesForTask -> surveyResponsesForTask
                        .stream()
                        .max(Comparator.comparing(sr -> sr.getTask().getCreatedAt()))
                        .orElseThrow())
                .toList();
    }


}
