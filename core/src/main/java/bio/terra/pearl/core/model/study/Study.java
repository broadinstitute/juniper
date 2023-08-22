package bio.terra.pearl.core.model.study;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.portal.Portal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Study extends BaseEntity {
    private String name;
    private String shortcode;
    private String pepperStudyName;
    @Builder.Default
    private Set<StudyEnvironment> studyEnvironments = new HashSet<>();
    @Builder.Default
    private Set<Portal> studyPortals = new HashSet<>();
}
