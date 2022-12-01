package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CascadeTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class EnrolleeService {
    private EnrolleeDao enrolleeDao;

    public EnrolleeService(EnrolleeDao enrolleeDao) {
        this.enrolleeDao = enrolleeDao;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return enrolleeDao.findOneByShortcode(shortcode);
    }

    public Enrollee create(Enrollee enrollee) {
        return enrolleeDao.create(enrollee);
    }

    public void delete(UUID enrolleeId, CascadeTree cascadeTree) {
        enrolleeDao.delete(enrolleeId);
    }

    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId, Set<CascadeProperty> cascade) {
        enrolleeDao.deleteByStudyEnvironmentId(studyEnvironmentId);
    }
}
