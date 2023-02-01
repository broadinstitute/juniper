package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class ConsentResponseService extends CrudService<ConsentResponse, ConsentResponseDao> {

    public ConsentResponseService(ConsentResponseDao dao) {
        super(dao);
    }
}
