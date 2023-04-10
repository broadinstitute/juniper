package bio.terra.pearl.populate.dto;

import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.populate.dto.site.SiteContentPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class PortalEnvironmentPopDto extends PortalEnvironment {
    private Set<String> participantUserFiles = new HashSet<>();
    private SurveyPopDto preRegSurveyDto;
    private SiteContentPopDto siteContentPopDto;
    private List<MailingListContact> mailingListContacts = new ArrayList<>();
}
