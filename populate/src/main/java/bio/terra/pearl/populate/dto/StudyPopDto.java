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
public class StudyPopDto extends Study {
    private List<String> surveyFiles;
    private List<String> consentFormFiles;
    private List<StudyEnvironmentPopDto> studyEnvironmentDtos = new ArrayList<>();

    /**
     * this allows us to pass a PopulateStudyDto to StudyService.create and have the specified environments
     * in studyEnvironmentDtos used for the creation.
     */
    public Set<StudyEnvironment> getStudyEnvironments() {
        return studyEnvironmentDtos.stream().collect(Collectors.toSet());
    }

}
