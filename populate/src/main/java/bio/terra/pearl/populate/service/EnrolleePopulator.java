package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeTree;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.populate.dto.EnrolleePopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class EnrolleePopulator extends Populator<Enrollee> {
    private EnrolleeService enrolleeService;
    private StudyEnvironmentService studyEnvironmentService;
    private ParticipantUserService participantUserService;

    public EnrolleePopulator(FilePopulateService filePopulateService,
                            ObjectMapper objectMapper,
                            EnrolleeService enrolleeService,
                             StudyEnvironmentService studyEnvironmentService,
                             ParticipantUserService participantUserService) {
        this.objectMapper = objectMapper;
        this.filePopulateService = filePopulateService;
        this.enrolleeService = enrolleeService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.participantUserService = participantUserService;
    }

    @Override
    public Enrollee populateFromString(String fileString, FilePopulateConfig config) throws IOException {
        EnrolleePopDto enrolleeDto = objectMapper.readValue(fileString, EnrolleePopDto.class);
        Optional<Enrollee> existingEnrollee = enrolleeService.findOneByShortcode(enrolleeDto.getShortcode());
        existingEnrollee.ifPresent(exEnrollee ->
                enrolleeService.delete(exEnrollee.getId(), CascadeTree.NONE)
        );
        StudyEnvironment attachedEnv = studyEnvironmentService
                .findByStudy(config.getStudyShortcode(), config.getEnvironmentName()).get();
        enrolleeDto.setStudyEnvironmentId(attachedEnv.getId());
        ParticipantUser attachedUser = participantUserService
                .findOne(enrolleeDto.getLinkedUsername(), config.getEnvironmentName()).get();
        enrolleeDto.setParticipantUserId(attachedUser.getId());
        Enrollee enrollee = enrolleeService.create(enrolleeDto);
        return enrollee;
    }
}

