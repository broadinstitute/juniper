package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.participant.MailingAddress;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressDao extends BaseJdbiDao<MailingAddress> {
    public MailingAddressDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<MailingAddress> getClazz() {
        return MailingAddress.class;
    }
}
