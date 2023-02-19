package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.study.PortalStudy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

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

    public Optional<PortalStudy> findStudyInPortal(String studyShortcode, UUID portalId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName
                                + " a join study on a.study_id = study.id"
                                + " where study.shortcode = :studyShortcode and a.portal_id = :portalId")
                        .bind("studyShortcode", studyShortcode)
                        .bind("portalId", portalId)
                        .mapTo(clazz)
                        .findOne());
    }

    public void deleteByPortalId(UUID portalId) {
        deleteByProperty("portal_id", portalId);
    }
}
