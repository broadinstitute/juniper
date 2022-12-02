package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.study.StudyEnvironment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StudyEnvironmentPopDto extends StudyEnvironment {
    private List<String> enrolleeFiles;
}
