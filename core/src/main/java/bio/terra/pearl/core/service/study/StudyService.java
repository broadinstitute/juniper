package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyService extends CrudService<Study, StudyDao> {
    private StudyEnvironmentService studyEnvironmentService;
    private PortalStudyService portalStudyService;

    public enum AllowedCascades implements CascadeProperty {
        STUDY_ENVIRONMENTS
    }

    public StudyService(StudyDao studyDao, StudyEnvironmentService studyEnvironmentService,
                        PortalStudyService portalStudyService) {
        super(studyDao);
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalStudyService = portalStudyService;
    }

    public Optional<Study> findByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }
    public Optional<Study> findByStudyEnvironmentId(UUID studyEnvId) {
        return dao.findByStudyEnvironmentId(studyEnvId);
    }
    public List<Study> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

    @Transactional
    public Study create(Study study) {
        Study newStudy = dao.create(study);
        study.getStudyEnvironments().forEach(studyEnv -> {
            studyEnv.setStudyId(newStudy.getId());
            StudyEnvironment newEnv = studyEnvironmentService.create(studyEnv);
            newStudy.getStudyEnvironments().add(newEnv);
        });
        return newStudy;
    }

    public List<Study> findWithPreregContent(String portalShortcode, EnvironmentName envName) {
        return dao.findWithPreregContent(portalShortcode, envName);
    }

    @Transactional
    public void delete(UUID studyId, Set<CascadeProperty> cascades) {
        studyEnvironmentService.deleteByStudyId(studyId, cascades);
        dao.delete(studyId);
    }

    @Transactional
    public void deleteOrphans(List<UUID> studyIds, Set<CascadeProperty> cascades) {
        studyIds.stream().forEach(studyId -> {
            if (portalStudyService.findByStudyId(studyId).size() == 0) {
                delete(studyId, cascades);
            }
        });
    }
}
