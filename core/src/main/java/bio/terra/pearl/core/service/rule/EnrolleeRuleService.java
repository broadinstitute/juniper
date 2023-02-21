package bio.terra.pearl.core.service.rule;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.participant.ProfileService;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeRuleService {
    private ProfileService profileService;


    public EnrolleeRuleService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public EnrolleeRuleData fetchData(Enrollee enrollee) {
        return new EnrolleeRuleData(enrollee, profileService.find(enrollee.getProfileId()).orElse(null));
    }
}
