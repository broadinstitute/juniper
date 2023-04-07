package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.MailingAddressDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService extends CrudService<Profile, ProfileDao> {
    private MailingAddressDao mailingAddressDao;

    public ProfileService(ProfileDao profileDao, MailingAddressDao mailingAddressDao) {
        super(profileDao);
        this.mailingAddressDao = mailingAddressDao;
    }

    @Transactional
    public Profile create(Profile profile) {
        MailingAddress newAddress = profile.getMailingAddress() != null ? profile.getMailingAddress() : new MailingAddress();
        newAddress = mailingAddressDao.create(newAddress);
        profile.setMailingAddressId(newAddress.getId());
        Profile newProfile = dao.create(profile);
        newProfile.setMailingAddress(newAddress);
        return newProfile;
    }

    public Optional<Profile> loadWithMailingAddress(UUID profileId) {
        return dao.loadWithMailingAddress(profileId);
    }

    @Transactional
    public Profile updateWithMailingAddress(Profile profile) {
        return dao.updateWithMailingAddress(profile);
    }

    @Transactional
    public void delete(UUID profileId, Set<CascadeProperty> cascade) {
        Profile profile = dao.find(profileId).get();
        dao.delete(profileId);
        if (profile.getMailingAddressId() != null) {
            mailingAddressDao.delete(profile.getMailingAddressId());
        }
    }
}
