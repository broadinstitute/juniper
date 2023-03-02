package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ProfileDao extends BaseMutableJdbiDao<Profile> {
    private MailingAddressDao mailingAddressDao;
    public ProfileDao(Jdbi jdbi, MailingAddressDao mailingAddressDao) {
        super(jdbi);
        this.mailingAddressDao = mailingAddressDao;
    }

    @Override
    protected Class<Profile> getClazz() {
        return Profile.class;
    }

    public Optional<Profile> loadWithMailingAddress(UUID profileId) {
        Optional<Profile> profileOpt = find(profileId);
        profileOpt.ifPresent(profile -> {
            if (profile.getMailingAddressId() != null) {
                profile.setMailingAddress(mailingAddressDao.find(profile.getMailingAddressId()).get());
            }
        });
        return profileOpt;
    }

    /** updates the profile and also updates the attached mailing address, if any */
    public Profile updateWithMailingAddress(Profile profile) {
        MailingAddress mailingAddress = profile.getMailingAddress();
        if (mailingAddress != null) {
            if (profile.getMailingAddressId() == null) {
                // we've added a new mailing address
                mailingAddress = mailingAddressDao.create(profile.getMailingAddress());
                profile.setMailingAddressId(mailingAddress.getId());
            } else {
                mailingAddressDao.update(mailingAddress);
            }
        }
        Profile updatedProfile = update(profile);
        updatedProfile.setMailingAddress(mailingAddress);
        return updatedProfile;
    }
}
