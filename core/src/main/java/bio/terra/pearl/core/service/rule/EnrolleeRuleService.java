package bio.terra.pearl.core.service.rule;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ProfileService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EnrolleeRuleService {
    private ProfileService profileService;
    private EnrolleeService enrolleeService;


    public EnrolleeRuleService(ProfileService profileService, @Lazy EnrolleeService enrolleeService) {
        this.profileService = profileService;
        this.enrolleeService = enrolleeService;
    }

    public EnrolleeRuleData fetchProfile(Enrollee enrollee) {
        return new EnrolleeRuleData(enrollee,
                profileService.loadWithMailingAddress(enrollee.getProfileId()).orElse(null));
    }

    /**
     * useful for bulk-fetching enrollees for processing
     * this isn't terribly optimized -- we could do the join in the DB.  But this is assuming that the number of enrollees
     *  is ~5-30, not 1000+, and so the main goal is just making sure we only do 2 total DB roundtrips
     */
    public List<EnrolleeRuleData> fetchAllWithProfile(List<UUID> enrolleeIds) {
        List<Enrollee> enrollees = enrolleeService.findAll(enrolleeIds);
        List<Profile> profiles = profileService.findAll(enrollees.stream().map(Enrollee::getProfileId).toList());
        List<EnrolleeRuleData> ruleData = enrollees.stream().map(enrollee -> {
            Profile profile = profiles.stream().filter(p ->
                    p.getId().equals(enrollee.getProfileId())).findFirst().orElse(null);
            return new EnrolleeRuleData(enrollee, profile);
        }).toList();
        return ruleData;
    }
}
