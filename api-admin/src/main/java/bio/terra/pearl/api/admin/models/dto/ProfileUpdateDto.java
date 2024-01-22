package bio.terra.pearl.api.admin.models.dto;

import bio.terra.pearl.core.model.participant.Profile;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ProfileUpdateDto {
  private String justification;
  private Profile profile;
}
