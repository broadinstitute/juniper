package bio.terra.pearl.populate.dto.participant;

import bio.terra.pearl.core.model.participant.Family;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor
public class FamilyPopDto extends Family {
    private String probandShortcode;
    private List<String> memberShortcodes;
    private List<FamilyRelationship> relations;

    public static record FamilyRelationship(String shortcode, String targetShortcode, String relationship) {
    }
}
