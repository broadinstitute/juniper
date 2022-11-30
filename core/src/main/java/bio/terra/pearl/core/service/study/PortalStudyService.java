package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.PortalStudyDao;
import bio.terra.pearl.core.model.study.PortalStudy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PortalStudyService {
    private PortalStudyDao portalStudyDao;
    public PortalStudyService(PortalStudyDao portalStudyDao) {
        this.portalStudyDao = portalStudyDao;
    }
    public List<PortalStudy> findByPortalId(UUID portalId) {
        return portalStudyDao.findByPortalId(portalId);
    }

    public PortalStudy create(UUID portalId, UUID studyId) {
        PortalStudy portalStudy = PortalStudy.builder()
                .portalId(portalId).studyId(studyId).build();
        return portalStudyDao.create(portalStudy);
    }

    public List<PortalStudy> findByStudyId(UUID studyId) {
        return portalStudyDao.findByStudyId(studyId);
    }

    @Transactional
    public void deleteByPortalId(UUID portalId) {
        portalStudyDao.deleteByPortalId(portalId);
    }
}
