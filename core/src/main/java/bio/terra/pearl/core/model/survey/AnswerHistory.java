package bio.terra.pearl.core.model.survey;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * used to record any submission of an answer by any user.  While Answers may be updated and/or deleted, AnswerHistory
 * should be immutable and never destroyed
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AnswerHistory extends Answer {
}
