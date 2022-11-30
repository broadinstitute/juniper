package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.portal.Portal;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
public class PopulatePortalDto extends Portal {
    private Set<String> populateStudyFiles = new HashSet<>();
    private Set<String> participantUserFiles = new HashSet<>();
}
