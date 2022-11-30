package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.service.CascadeTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EnrolleeService {
    private EnrolleeDao enrolleeDao;

    public EnrolleeService(EnrolleeDao enrolleeDao) {
        this.enrolleeDao = enrolleeDao;
    }

    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId, CascadeTree cascade) {
        enrolleeDao.deleteByStudyEnvironmentId(studyEnvironmentId, cascade);
    }
}
