package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.dao.search.EnrolleeSearchExpressionDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.service.export.formatters.module.*;
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

    public List<EnrolleeExportData> loadEnrolleeExportData(UUID studyEnvironmentId, ExportOptionsWithExpression exportOptions) {
        Study study = studyService.findByStudyEnvironmentId(studyEnvironmentId).orElseThrow();
        return loadEnrolleesForExport(
                study,
                studyEnvironmentConfigService.findByStudyEnvironmentId(studyEnvironmentId),
                loadEnrollees(studyEnvironmentId, exportOptions.getFilterExpression(), exportOptions.getRowLimit()));
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

    List<SurveyType> SURVEY_TYPE_EXPORT_ORDER = List.of(SurveyType.CONSENT, SurveyType.RESEARCH, SurveyType.OUTREACH);

    /**
     * returns a ModuleExportInfo for each unique survey stableId that has ever been attached to the studyEnvironment
     * If multiple versions of a survey have been attached, those will be consolidated into a single ModuleExportInfo
     */
    protected List<SurveyFormatter> generateSurveyModules(ExportOptions exportOptions, UUID studyEnvironmentId, List<EnrolleeExportData> enrolleeExportData) {
        List<SurveyFormatter> moduleFormatters = new ArrayList<>();
        // now add the pre-enrollment survey (if it exists)
        StudyEnvironment studyEnvironment = studyEnvironmentService.find(studyEnvironmentId).orElseThrow();
        if (studyEnvironment.getPreEnrollSurveyId() != null) {
            Survey preEnrollSurvey = surveyService.find(studyEnvironment.getPreEnrollSurveyId()).orElseThrow();
            List<SurveyQuestionDefinition> preEnrollSurveyQuestionDefinitions = surveyQuestionDefinitionDao.findAllBySurveyIds(List.of(preEnrollSurvey.getId()));
            moduleFormatters.add(new SurveyFormatter(
                    exportOptions,
                    preEnrollSurvey.getStableId(),
                    List.of(preEnrollSurvey),
                    preEnrollSurveyQuestionDefinitions,
                    enrolleeExportData,
                    objectMapper));
        }

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

    protected List<EnrolleeExportData> loadEnrolleesForExport(Study study,
                                                              StudyEnvironmentConfig config,
                                                              List<Enrollee> enrollees) {
        // for now, load each enrollee individually.  Later we'll want more sophisticated batching strategies
        return enrollees
                .stream()
                .map(enrollee -> loadEnrolleeData(study, config, enrollee))
                .toList();
    }

    protected EnrolleeExportData loadEnrolleeData(Study study, StudyEnvironmentConfig config,
                                                  Enrollee enrollee) {

        List<EnrolleeRelation> relations = loadRelations(config, enrollee);
        List<ParticipantUser> proxies = loadProxyUsers(config, relations);
        return new EnrolleeExportData(
                study,
                enrollee,
                participantUserService.find(enrollee.getParticipantUserId()).orElseThrow(),
                profileService.loadWithMailingAddress(enrollee.getProfileId()).get(),
                answerDao.findByEnrolleeId(enrollee.getId()),
                participantTaskService.findByEnrolleeId(enrollee.getId()),
                surveyResponseService.findByEnrolleeId(enrollee.getId()),
                kitRequestService.findByEnrollee(enrollee),
                relations,
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


}
