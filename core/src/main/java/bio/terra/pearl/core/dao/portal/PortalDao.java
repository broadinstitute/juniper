package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.study.PortalStudyDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.PortalStudy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalDao extends BaseJdbiDao<Portal> {
    private PortalEnvironmentDao portalEnvironmentDao;
    private PortalStudyDao portalStudyDao;
    private StudyDao studyDao;

    @Override
    protected Class<Portal> getClazz() {
        return Portal.class;
    }

    public PortalDao(Jdbi jdbi, PortalEnvironmentDao portalEnvironmentDao,
                     PortalStudyDao portalStudyDao, StudyDao studyDao) {
        super(jdbi);
        this.portalEnvironmentDao = portalEnvironmentDao;
        this.portalStudyDao = portalStudyDao;
        this.studyDao = studyDao;
    }

    public Optional<Portal> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    /** return a full portal object, with all children, excepting users and participants, and images
     * This isn't terribly optimized yet */
    public Optional<Portal> findOneByShortcodeFullLoad(String shortcode, String language) {
        Optional<Portal> portalOpt = findByProperty("shortcode", shortcode);
        portalOpt.ifPresent(portal -> {
            List<PortalEnvironment> portalEnvs = portalEnvironmentDao.findByPortal(portal.getId());
            for (PortalEnvironment portalEnv : portalEnvs) {
                portal.getPortalEnvironments().add(
                        portalEnvironmentDao.loadOneWithSiteContent(shortcode,
                                portalEnv.getEnvironmentName(), language).get()
                );
            }
            List<PortalStudy> portalStudies = portalStudyDao.findByPortalId(portal.getId());
            for (PortalStudy portalStudy : portalStudies) {
                portalStudy.setStudy(studyDao.findOneFullLoad(portalStudy.getStudyId()).get());
                portal.getPortalStudies().add(portalStudy);
            }
        });
        return portalOpt;
    }

    public List<Portal> findByAdminUserId(UUID userId) {
        // for now, return all portals
        return findAll();
    }
}
