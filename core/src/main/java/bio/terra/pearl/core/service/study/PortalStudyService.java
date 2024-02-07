package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.PortalStudyDao;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.service.ImmutableEntityService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalStudyService extends ImmutableEntityService<PortalStudy, PortalStudyDao> {

    public PortalStudyService(PortalStudyDao portalStudyDao) {
        super(portalStudyDao);
    }

    public List<PortalStudy> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

    public PortalStudy create(UUID portalId, UUID studyId) {
        PortalStudy portalStudy = PortalStudy.builder()
                .portalId(portalId).studyId(studyId).build();
        return dao.create(portalStudy);
    }

    public List<PortalStudy> findByStudyId(UUID studyId) {
        return dao.findByStudyId(studyId);
    }

    public List<PortalStudy> findByEnrollee(String enrolleeShortcode) {
        return dao.findByEnrollee(enrolleeShortcode);
    }

    public Optional<PortalStudy> findStudyInPortal(String shortcode, UUID portalId) {
        return dao.findStudyInPortal(shortcode, portalId);
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        dao.deleteByPortalId(portalId);
    }

    @Transactional
    public void deleteByStudyId(UUID studyId) {
        dao.deleteByStudyId(studyId);
    }
}
