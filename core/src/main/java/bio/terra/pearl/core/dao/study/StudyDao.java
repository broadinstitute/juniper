package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyDao  extends BaseJdbiDao<Study> {
    private StudyEnvironmentDao studyEnvironmentDao;

    @Override
    protected Class<Study> getClazz() {
        return Study.class;
    }

    public StudyDao(Jdbi jdbi, StudyEnvironmentDao studyEnvironmentDao) {
        super(jdbi);
        this.studyEnvironmentDao = studyEnvironmentDao;
    }

    public Optional<Study> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public List<Study> findByPortal(String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName
                                + " a join portal_study on a.id = portal_study.study_id "
                                + " join portal on portal_study.portal_id = portal.id"
                                + " where portal.shortcode = :portalShortcode")
                        .bind("portalShortcode", portalShortcode)
                        .mapTo(clazz)
                        .list()
        );
    }

    public Optional<Study> findOneFullLoad(UUID id) {
        Optional<Study> studyOpt = find(id);
        studyOpt.ifPresent(study -> {
            List<StudyEnvironment> studyEnvs = studyEnvironmentDao.findByStudy(id);
            /**
             * Iterate through each environment and load the content.  This could be optimized further,
             * by batching queries across environments (or by only fetching one environment at a time from the UX)
             * but since it's used only by the admin UI, speed isn't as important yet.
             */
            for (StudyEnvironment studyEnv: studyEnvs) {
                studyEnv = studyEnvironmentDao.loadWithAllContent(studyEnv);
                study.getStudyEnvironments().add(studyEnv);
            }
        });
        return studyOpt;
    }

    /**
     * returns all the studies associated with the given portal for the given environment
     * So, for example, if a portal has two studies, this might return the 'sandbox' environment for
     * both studies
     */
    public List<Study> findWithPreregContent(String portalShortcode, EnvironmentName envName) {
        List<Study> studies = findByPortal(portalShortcode);
        List<StudyEnvironment> studyEnvs =  studyEnvironmentDao.findWithPreregContent(portalShortcode, envName);
        for (Study study : studies) {
            Optional<StudyEnvironment> studyEnvOpt = studyEnvs.stream()
                    .filter(studyEnv -> studyEnv.getStudyId().equals(study.getId())).findFirst();
            studyEnvOpt.ifPresent(studyEnv -> study.getStudyEnvironments().add(studyEnv));
        }
        return studies;
    }
}
