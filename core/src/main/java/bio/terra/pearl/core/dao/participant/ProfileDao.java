package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.Profile;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
}
