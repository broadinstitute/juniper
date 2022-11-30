package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter @Setter @NoArgsConstructor
public class PopulateStudyDto extends Study {
    private List<PopulateStudyEnvironmentDto> studyEnvironmentDtos = new ArrayList<>();
    public Set<StudyEnvironment> getStudyEnvironments() {
        return studyEnvironmentDtos.stream().collect(Collectors.toSet());
    }
}
