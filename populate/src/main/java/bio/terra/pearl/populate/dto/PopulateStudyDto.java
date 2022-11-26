package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.Study;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class PopulateStudyDto extends Study {
    private List<String> participantFiles;
}
