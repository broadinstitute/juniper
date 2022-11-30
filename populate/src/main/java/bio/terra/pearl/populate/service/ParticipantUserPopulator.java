package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class ParticipantUserPopulator extends Populator<ParticipantUser> {
    private ParticipantUserService participantUserService;

    public ParticipantUserPopulator(ObjectMapper objectMapper, ParticipantUserService participantUserService, FilePopulateService filePopulateService) {
        this.participantUserService = participantUserService;
        this.filePopulateService = filePopulateService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ParticipantUser populateFromString(String fileContent, FilePopulateConfig config) throws IOException {
        ParticipantUser userDto = objectMapper.readValue(fileContent, ParticipantUser.class);
        Optional<ParticipantUser> existingUser = participantUserService
                .findOne(userDto.getUsername(), userDto.getEnvironmentName());
        existingUser.ifPresent(exUser ->
                participantUserService.delete(exUser.getId())
        );
        ParticipantUser participantUser = participantUserService.create(userDto);

        return participantUser;
    }
}
