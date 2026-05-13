package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.SurveyResponseWithTaskDto;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder @AllArgsConstructor
public class EnrolleeExportData {
    private Study study;
    private Enrollee enrollee;
    private ParticipantUser participantUser;
    private Profile profile;
    private List<Profile> proxyProfiles;
    private List<Answer> answers;
    private List<ParticipantTask> tasks;
    private List<SurveyResponseWithTaskDto> responses;
    private List<KitRequestDto> kitRequests;
    private List<EnrolleeRelation> enrolleeRelations;
    private List<Family> families;
    private List<ParticipantUser> proxyUsers;
    private List<ParticipantFile> participantFiles;
}
