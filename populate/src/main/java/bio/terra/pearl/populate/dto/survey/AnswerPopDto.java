package bio.terra.pearl.populate.dto.survey;

import bio.terra.pearl.core.model.survey.Answer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter @Setter @NoArgsConstructor @SuperBuilder
public class AnswerPopDto extends Answer {
    private JsonNode objectJsonValue;
    /** convenience field for FILE_UPLOAD answers: list of file names to include in the objectValue JSON array */
    private List<String> fileNames;
}
