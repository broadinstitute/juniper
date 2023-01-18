package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class PortalEnvironmentPopDto extends PortalEnvironment {
    private Set<String> participantUserFiles = new HashSet<>();
    private SurveyPopDto preRegSurveyDto;
    private String siteContentFile;
}
