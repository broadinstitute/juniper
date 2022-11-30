package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.MailingAddressDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfileService {
    private ProfileDao profileDao;
    private MailingAddressDao mailingAddressDao;

    public ProfileService(ProfileDao profileDao, MailingAddressDao mailingAddressDao) {
        this.profileDao = profileDao;
        this.mailingAddressDao = mailingAddressDao;
    }

    @Transactional
    public Profile create(Profile profile) {
        MailingAddress newAddress = null;
        if (profile.getMailingAddress() != null) {
            newAddress = mailingAddressDao.create(profile.getMailingAddress());
            profile.setMailingAddressId(newAddress.getId());
        }
        Profile newProfile = profileDao.create(profile);
        newProfile.setMailingAddress(newAddress);
        return newProfile;
    }

    @Transactional
    public void delete(UUID profileId) {
        Profile profile = profileDao.find(profileId).get();
        profileDao.delete(profileId);
        if (profile.getMailingAddressId() != null) {
            mailingAddressDao.delete(profile.getMailingAddressId());
        }
    }
}
