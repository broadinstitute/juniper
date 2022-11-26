package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.PortalStudy;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PortalStudyDao extends BaseJdbiDao<PortalStudy> {
    public PortalStudyDao(Jdbi jdbi) {
        super(jdbi);
    }
    @Override
    protected Class<PortalStudy> getClazz() {
        return PortalStudy.class;
    }

    public List<PortalStudy> findByStudyId(UUID studyId) {
        return findAllByProperty("study_id", studyId);
    }

    public List<PortalStudy> findByPortalId(UUID portalId) {
        return findAllByProperty("portal_id", portalId);
    }
    public void deleteByPortalId(UUID portalId) {
        deleteByUuidProperty("portal_id", portalId);
    }


}
