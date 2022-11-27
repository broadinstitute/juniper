package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.Study;
import bio.terra.pearl.core.service.CascadeTree;
import bio.terra.pearl.core.service.StudyService;
import bio.terra.pearl.populate.dto.PopulateStudyDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class StudyPopulator implements Populator<Study> {
    private ObjectMapper objectMapper;
    private StudyService studyService;
    private FilePopulateService filePopulateService;

    public StudyPopulator(ObjectMapper objectMapper, StudyService studyService, FilePopulateService filePopulateService) {
        this.studyService = studyService;
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

    protected Study populateFromString(String studyContent, FilePopulateConfig config)  throws IOException {
        PopulateStudyDto studyDto = objectMapper.readValue(studyContent, PopulateStudyDto.class);
        Optional<Study> existingStudy = studyService.findByShortcode(studyDto.getShortcode());
        existingStudy.ifPresent(study ->
            studyService.delete(study.getId(), CascadeTree.NONE)
        );
        Study newStudy = studyService.create(studyDto);
        return newStudy;
    }
}
