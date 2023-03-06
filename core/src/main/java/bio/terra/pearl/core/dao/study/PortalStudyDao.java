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

    /** gets a list of PortalStudies corresponding to an Enrollee.  Enrollees are specific to a single Study,
     * so this will only return multiple results if that Study is in multiple Portals
     */
    public List<PortalStudy> findByEnrollee(String enrolleeShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName
                                + " a join study on a.study_id = study.id"
                                + " join study_environment on study.id = study_environment.study_id"
                                + " join enrollee on study_environment.id = enrollee.study_environment_id"
                                + " where enrollee.shortcode = :enrolleeShortcode")
                        .bind("enrolleeShortcode", enrolleeShortcode)
                        .mapTo(clazz)
                        .list());
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
