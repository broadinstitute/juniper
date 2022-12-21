package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.populate.dto.StudyEnvironmentPopDto;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyPopulator extends Populator<Study> {
    private StudyService studyService;
    private EnrolleePopulator enrolleePopulator;
    private SurveyPopulator surveyPopulator;

    public StudyPopulator(ObjectMapper objectMapper, StudyService studyService, FilePopulateService filePopulateService, EnrolleePopulator enrolleePopulator, SurveyPopulator surveyPopulator) {
        this.studyService = studyService;
        this.enrolleePopulator = enrolleePopulator;
        this.surveyPopulator = surveyPopulator;
        this.filePopulateService = filePopulateService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public Study populate(String filePathName) throws IOException {
        FilePopulateConfig config = new FilePopulateConfig(filePathName);
        return populate(config.getRootFileName(), config);
    }

    public Study populate(String studyFileName, FilePopulateConfig config) throws IOException {
        String portalFileString = filePopulateService.readFile(studyFileName, config);
        return populateFromString(portalFileString, config);
    }

    public Study populateFromString(String studyContent, FilePopulateConfig config)  throws IOException {
        StudyPopDto studyDto = objectMapper.readValue(studyContent, StudyPopDto.class);

        // delete the existing survey
        Optional<Study> existingStudy = studyService.findByShortcode(studyDto.getShortcode());
        existingStudy.ifPresent(study ->
            studyService.delete(study.getId(), new HashSet<>())
        );

        // first, populate the surveys themselves
        for (String surveyFile : studyDto.getSurveyFiles()) {
            surveyPopulator.populate(config.newFrom(surveyFile));
        }
        for (StudyEnvironmentPopDto studyEnv : studyDto.getStudyEnvironmentDtos()) {
            for (int i = 0; i < studyEnv.getConfiguredSurveyDtos().size(); i++) {
                StudyEnvironmentSurveyPopDto configSurveyDto = studyEnv.getConfiguredSurveyDtos().get(i);
                StudyEnvironmentSurvey configSurvey = surveyPopulator.convertConfiguredSurvey(configSurveyDto, i);
                studyEnv.getConfiguredSurveys().add(configSurvey);
            }
            if (studyEnv.getPreRegSurveyDto() != null) {
                Survey preRegSurvey = surveyPopulator.fetchFromPopDto(studyEnv.getPreRegSurveyDto()).get();
                studyEnv.setPreRegSurveyId(preRegSurvey.getId());
            }
        }

        Study newStudy = studyService.create(studyDto);

        for (StudyEnvironmentPopDto studyEnv : studyDto.getStudyEnvironmentDtos()) {
            for (String enrolleeFile : studyEnv.getEnrolleeFiles()) {
                enrolleePopulator.populate(
                        config.newForStudy(enrolleeFile, newStudy.getShortcode(), studyEnv.getEnvironmentName())
                );
            }
        }
        return newStudy;
    }


}
