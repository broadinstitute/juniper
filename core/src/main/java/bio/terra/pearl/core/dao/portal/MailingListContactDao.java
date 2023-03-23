package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.portal.MailingListContact;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class MailingListContactDao extends BaseJdbiDao<MailingListContact> {
    public MailingListContactDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<MailingListContact> getClazz() {
        return MailingListContact.class;
    }
}
