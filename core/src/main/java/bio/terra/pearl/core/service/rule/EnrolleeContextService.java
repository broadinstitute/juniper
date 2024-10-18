package bio.terra.pearl.core.service.rule;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class EnrolleeContextService {
    private final ProfileService profileService;
    private final EnrolleeService enrolleeService;
    private final ParticipantUserService participantUserService;


    public EnrolleeContextService(ProfileService profileService, @Lazy EnrolleeService enrolleeService,
                                  ParticipantUserService participantUserService) {
        this.profileService = profileService;
        this.enrolleeService = enrolleeService;
        this.participantUserService = participantUserService;
    }

    public EnrolleeContext fetchData(Enrollee enrollee) {
        return new EnrolleeContext(enrollee,
                profileService.loadWithMailingAddress(enrollee.getProfileId()).orElse(null),
                participantUserService.find(enrollee.getParticipantUserId()).orElseThrow(() -> new IllegalStateException("no participant user for enrollee")));
    }

    /**
     * useful for bulk-fetching enrollees for processing
     * this isn't terribly optimized -- we could do the join in the DB.  But this is assuming that the number of enrollees
     *  is ~5-30, not 1000+, and so the main goal is just making sure we only do 4 total DB roundtrips.
     *
     *  This returns the context for each enrollee in the order of the input list
     */
    public List<EnrolleeContext> fetchData(List<UUID> enrolleeIds) {
        if (enrolleeIds.isEmpty()) {
            return List.of();
        }
        List<Enrollee> enrollees = enrolleeService.findAllPreserveOrder(enrolleeIds);
        List<Profile> profiles = profileService.findAllWithMailingAddressPreserveOrder(enrollees.stream().map(Enrollee::getProfileId).toList());
        List<ParticipantUser> users = participantUserService.findAllPreserveOrder(enrollees.stream().map(Enrollee::getParticipantUserId).toList());
        List<EnrolleeContext> ruleData = IntStream.range(0, enrollees.size()).mapToObj(i ->
                new EnrolleeContext(enrollees.get(i), profiles.get(i), users.get(i))
        ).toList();
        return ruleData;
    }
}
