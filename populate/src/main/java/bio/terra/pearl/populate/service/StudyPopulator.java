package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.populate.dto.StudyEnvironmentPopDto;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyBatchPopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

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
            for (SurveyBatchPopDto batchPopDto : studyEnv.getSurveyBatchDtos()) {
                surveyPopulator.attachSurveyBatchSurveys(batchPopDto);
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
