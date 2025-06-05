package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.participant.Profile;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public List<Profile> loadAllWithMailingAddress(List<UUID> profileIds) {
        List<Profile> profiles = findAll(profileIds);
        profiles.forEach(profile -> {
            if (profile.getMailingAddressId() != null) {
                profile.setMailingAddress(mailingAddressDao.find(profile.getMailingAddressId()).get());
            }
        });
        return profiles;
    }

    public List<Profile> findAllByEnrolleeIds(List<UUID> enrolleeIds) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                SELECT p.* FROM profile p INNER JOIN enrollee e ON p.id = e.profile_id
                                WHERE e.id IN (<enrolleeIds>)
                                """)
                        .bindList("enrolleeIds", enrolleeIds)
                        .mapToBean(Profile.class)
                        .list()
        );
    }
}
