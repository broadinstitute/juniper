package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.address.MailingAddress;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class MailingAddressDao extends BaseMutableJdbiDao<MailingAddress> {
    public MailingAddressDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<MailingAddress> getClazz() {
        return MailingAddress.class;
    }
}
