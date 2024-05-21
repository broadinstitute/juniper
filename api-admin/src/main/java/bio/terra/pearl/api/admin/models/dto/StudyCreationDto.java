package bio.terra.pearl.api.admin.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StudyCreationDto {
  private String shortcode;
  private String name;
  private StudyTemplate template;

  public StudyCreationDto(String shortcode, String name) {
    this.shortcode = shortcode;
    this.name = name;
  }

  public enum StudyTemplate {
    BASIC
  }
}
