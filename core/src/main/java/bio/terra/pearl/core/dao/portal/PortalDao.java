package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.admin.PortalAdminUserDao;
import bio.terra.pearl.core.dao.study.PortalStudyDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.model.admin.PortalAdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.PortalStudy;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PortalDao extends BaseMutableJdbiDao<Portal> {
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
     * matches on either a shortcode or hostname. if it's a hostname, the hostname should not include the tld.
     * e.g. it should be "somedomain", not "somedomain.org"
     * This is to handle requests from the client where the client does not necessarily
     * know whether the string from a url is a custom domain or the shortcode.
     *
     * The query has a limit of 1 put on it to cover the case where multiple environments of a portal might have the same hostname
     * e.g. we might want both the irb and live environments of [[customer]] to be at sandbox.customer.org and customer.org
     *
     * This likewise means we will have to put substantial restrictions around who/how the "participant_hostname" field can be edited,
     * otherwise, a new portal could inadvertently put "ourhealthstudy.org" as their participant_hostname,
     * and suddenly start stealing ourhealth's traffic.
     */
    public Optional<Portal> findOneByShortcodeOrHostname(String shortcodeOrHostname) {
        Optional<Portal> portal = findOneByShortcode(shortcodeOrHostname)
                .or(() -> jdbi.withHandle(handle ->
                    handle.createQuery("select " + prefixedGetQueryColumns("p") + """
                                        from portal p
                                       inner join portal_environment pe on pe.portal_id = p.id
                                       inner join portal_environment_config pec on pec.id = pe.portal_environment_config_id 
                                       where pec.participant_hostname like :hostnameLike
                                       order by p.created_at asc
                                       limit 1
                                           """)
                            .bind("hostnameLike", shortcodeOrHostname + ".%")
                            .mapTo(Portal.class)
                            .findOne()
            )
        );
        return portal;
    }

    /**
     * hydrates the passed-in portal object, with all children, excepting users, participants, and images
     * This isn't terribly optimized yet
     * */
    public Portal fullLoad(Portal portal, String language) {
        List<PortalEnvironment> portalEnvs = portalEnvironmentDao.findByPortalWithConfigs(portal.getId());
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

    public void attachPortalEnvironments(List<Portal> portals) {
        for(Portal portal : portals) {
            UUID portalId = portal.getId();
            List<PortalEnvironment> portalEnvironments = portalEnvironmentDao.findByPortalWithConfigs(portalId);
            portal.getPortalEnvironments().addAll(portalEnvironments);
        }
    }

    public void attachStudies(List<Portal> portals) {
        List<UUID> portalIds = portals.stream().map(portal -> portal.getId()).toList();
        List<PortalStudy> portalStudies = portalStudyDao.findByPortalIds(portalIds);
        portalStudyDao.attachStudies(portalStudies);
        for(Portal portal : portals) {
            List<PortalStudy> matches = portalStudies.stream().filter(portalStudy -> portalStudy.getPortalId().equals(portal.getId())).toList();
            portal.getPortalStudies().addAll(matches);
        }
    }

    public List<Portal> findByAdminUserId(UUID userId) {
        List<PortalAdminUser> portalAdmins = portalAdminUserDao.findByUserId(userId);
        return findAll(portalAdmins.stream().map(PortalAdminUser::getPortalId).toList());
    }

    public Optional<Portal> findByPortalEnvironmentId(UUID portalEnvironmentId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("p") + " from " + tableName + " p " +
                                " inner join portal_environment pe on pe.portal_id = p.id " +
                                " where pe.id = :portalEnvironmentId " +
                                " limit 1")
                        .bind("portalEnvironmentId", portalEnvironmentId)
                        .mapTo(clazz)
                        .findOne());
    }
    
}
