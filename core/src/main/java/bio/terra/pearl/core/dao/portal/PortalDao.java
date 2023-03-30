package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.dao.admin.PortalAdminUserDao;
import bio.terra.pearl.core.dao.study.PortalStudyDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
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
    private PortalAdminUserDao portalAdminUserDao;

    @Override
    protected Class<Portal> getClazz() {
        return Portal.class;
    }

    public PortalDao(Jdbi jdbi, PortalEnvironmentDao portalEnvironmentDao,
                     PortalStudyDao portalStudyDao, StudyDao studyDao, PortalAdminUserDao portalAdminUserDao) {
        super(jdbi);
        this.portalEnvironmentDao = portalEnvironmentDao;
        this.portalStudyDao = portalStudyDao;
        this.studyDao = studyDao;
        this.portalAdminUserDao = portalAdminUserDao;
    }

    public Optional<Portal> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    /**
     * hydrates the passed-in portal object, with all children, excepting users, participants, and images
     * This isn't terribly optimized yet
     * */
    public Portal fullLoad(Portal portal, String language) {
        List<PortalEnvironment> portalEnvs = portalEnvironmentDao.findByPortal(portal.getId());
        for (PortalEnvironment portalEnv : portalEnvs) {
            portal.getPortalEnvironments().add(
                    portalEnvironmentDao.loadWithSiteContent(portal.getShortcode(),
                            portalEnv.getEnvironmentName(), language).get()
            );
        }
        List<PortalStudy> portalStudies = portalStudyDao.findByPortalId(portal.getId());
        for (PortalStudy portalStudy : portalStudies) {
            portalStudy.setStudy(studyDao.findOneFullLoad(portalStudy.getStudyId()).get());
            portal.getPortalStudies().add(portalStudy);
        }
        return portal;
    }

    public List<Portal> findByAdminUserId(UUID userId) {
        List<PortalAdminUser> portalAdmins = portalAdminUserDao.findByUserId(userId);
        return findAll(portalAdmins.stream().map(PortalAdminUser::getPortalId).toList());
    }
}
