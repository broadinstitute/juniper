package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.MailingListContactDao;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class MailingListContactService extends CrudService<MailingListContact, MailingListContactDao> {
    public MailingListContactService(MailingListContactDao dao) {
        super(dao);
    }
}
