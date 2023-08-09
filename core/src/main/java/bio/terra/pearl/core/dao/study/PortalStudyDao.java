package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.study.PortalStudy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.model.study.Study;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalStudyDao extends BaseJdbiDao<PortalStudy> {
    private StudyDao studyDao;
    public PortalStudyDao(Jdbi jdbi, StudyDao studyDao) {
        super(jdbi);
        this.studyDao = studyDao;
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
    public List<PortalStudy> findByPortalIds(List<UUID> portalIds) {
        return findAllByPropertyCollection("portal_id", portalIds);
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

    public void attachStudies(List<PortalStudy> portalStudies) {
        List<UUID> ids = portalStudies.stream().map(ps -> ps.getStudyId()).toList();
        List<Study> studies = studyDao.findAll(ids);
        for(PortalStudy portalStudy : portalStudies) {
            portalStudy.setStudy(studies.stream().filter(study -> study.getId().equals(portalStudy.getStudyId()))
                    .findFirst().get());
        }
    }

    public void deleteByPortalId(UUID portalId) {
        deleteByProperty("portal_id", portalId);
    }
}
