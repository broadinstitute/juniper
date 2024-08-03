package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PortalEnvironmentChangeRecordPopDto extends PortalEnvironmentChangeRecord implements TimeShiftable {
    private Integer submittedHoursAgo;
    private String adminUsername;
    private JsonNode portalEnvironmentChangeJson;
}
