package bio.terra.pearl.core.model.search;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.Answer;
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
public class EnrolleeSearchResult {
    private Enrollee enrollee;
    private Profile profile;
    private MailingAddress mailingAddress;
    private final List<Answer> answers = new ArrayList<>();
}
