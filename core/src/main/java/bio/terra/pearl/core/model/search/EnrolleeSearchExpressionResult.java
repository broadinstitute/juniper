package bio.terra.pearl.core.model.search;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class EnrolleeSearchExpressionResult {
    private Enrollee enrollee;
    private Profile profile;
    private MailingAddress mailingAddress;
    private final List<Answer> answers = new ArrayList<>();
    private final List<ParticipantTask> tasks = new ArrayList<>();
    private final List<Family> families = new ArrayList<>();
    private KitRequest latestKit;
}
