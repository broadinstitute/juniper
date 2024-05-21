package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

@Service
public class PreEnrollmentResponseService extends CrudService<PreEnrollmentResponse, PreEnrollmentResponseDao> {
    public PreEnrollmentResponseService(PreEnrollmentResponseDao dao) {
        super(dao);
    }
}
