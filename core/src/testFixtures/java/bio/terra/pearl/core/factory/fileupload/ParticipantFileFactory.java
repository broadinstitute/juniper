package bio.terra.pearl.core.factory.fileupload;

import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.file.ParticipantFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ParticipantFileFactory {
    @Autowired
    public ParticipantFileService participantFileService;

    @Autowired
    public EnrolleeFactory enrolleeFactory;


    public ParticipantFile.ParticipantFileBuilder builder() {
        return ParticipantFile
                .builder()
                .externalFileId(UUID.randomUUID())
                .fileName("testFile.csv")
                .fileType("csv");
    }

    public ParticipantFile.ParticipantFileBuilder builderWithDependencies(Enrollee enrollee) {
        return builder()
                .enrolleeId(enrollee.getId())
                .creatingParticipantUserId(enrollee.getParticipantUserId());
    }

    public ParticipantFile.ParticipantFileBuilder builderWithDependencies(String testName) {
        return builderWithDependencies(enrolleeFactory.buildPersisted(testName));
    }

    public ParticipantFile buildPersisted(ParticipantFile.ParticipantFileBuilder builder) {
        return participantFileService.create(builder.build());
    }

    public ParticipantFile buildPersisted(String testName) {
        return buildPersisted(builderWithDependencies(testName));
    }

    public ParticipantFile buildPersisted(Enrollee enrollee) {
        return buildPersisted(builderWithDependencies(enrollee));
    }
}
