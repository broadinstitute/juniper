package bio.terra.pearl.core.model.study;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.portal.Portal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter @Setter @SuperBuilder
@NoArgsConstructor
public class Study extends BaseEntity {
    private String name;
    private String shortcode;
    @Builder.Default
    private List<StudyEnvironment> studyEnvironments = new ArrayList<>();
    @Builder.Default
    private List<Portal> studyPortals = new ArrayList<>();
}
