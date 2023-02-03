package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeSearchService {
    private EnrolleeDao enrolleeDao;

    public EnrolleeSearchService(EnrolleeDao enrolleeDao) {
        this.enrolleeDao = enrolleeDao;
    }
}
