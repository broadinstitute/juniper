package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.MailingAddressDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService extends DataAuditedService<Profile, ProfileDao> {
    private MailingAddressDao mailingAddressDao;

    public ProfileService(ProfileDao profileDao, MailingAddressDao mailingAddressDao, DataChangeRecordService dataChangeRecordService, ObjectMapper objectMapper) {
        super(profileDao, dataChangeRecordService, objectMapper);
        this.mailingAddressDao = mailingAddressDao;
    }

    @Transactional
    public Profile create(Profile profile, DataAuditInfo auditInfo) {
        MailingAddress newAddress = profile.getMailingAddress() != null ? profile.getMailingAddress() : new MailingAddress();
        newAddress = mailingAddressDao.create(newAddress);
        profile.setMailingAddressId(newAddress.getId());
        Profile newProfile = super.create(profile, auditInfo);
        newProfile.setMailingAddress(newAddress);
        return newProfile;
    }

    public Optional<Profile> loadWithMailingAddress(UUID profileId) {
        return dao.loadWithMailingAddress(profileId);
    }

    @Transactional
    public Profile updateWithMailingAddress(Profile profile, DataAuditInfo auditInfo) {
        Profile updatedProfile = this.update(profile, auditInfo);

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

        updatedProfile.setMailingAddress(mailingAddress);
        return updatedProfile;
    }
    @Transactional
    public void delete(UUID profileId, DataAuditInfo auditInfo) {
        Profile profile = dao.find(profileId).get();
        super.delete(profileId, auditInfo);
        if (profile.getMailingAddressId() != null) {
            mailingAddressDao.delete(profile.getMailingAddressId());
        }
    }

    @Override
    protected Profile processModelBeforeAuditing(Profile p) {
        // make sure that the audit is saving the mailing address as part of the profile
        if (Objects.isNull(p.getMailingAddress()) && Objects.nonNull(p.getMailingAddressId())) {
            mailingAddressDao.find(p.getMailingAddressId()).ifPresent(p::setMailingAddress);
        }
        return p;
    }
}
