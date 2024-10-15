package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class StudyService extends CrudService<Study, StudyDao> {
    private StudyEnvironmentService studyEnvironmentService;
    private PortalStudyService portalStudyService;
    private StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;

    public enum AllowedCascades implements CascadeProperty {
        STUDY_ENVIRONMENTS
    }

    public StudyService(StudyDao studyDao, StudyEnvironmentService studyEnvironmentService,
                        PortalStudyService portalStudyService, StudyEnvironmentKitTypeService studyEnvironmentKitTypeService) {
        super(studyDao);
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalStudyService = portalStudyService;
        this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
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
        try {
            Study newStudy = dao.create(study);
            study.getStudyEnvironments().forEach(studyEnv -> {
                studyEnv.setStudyId(newStudy.getId());
                StudyEnvironment newEnv = studyEnvironmentService.create(studyEnv);
                newStudy.getStudyEnvironments().add(newEnv);
            });
            return newStudy;
        } catch (Exception e) {
            if (e.getCause() instanceof PSQLException && e.getMessage().contains("study_shortcode_key")) {
                throw new IllegalArgumentException("A study with that shortcode already exists");
            } else {
                throw e;
            }
        }
    }

    public List<Study> findWithPreregContent(String portalShortcode, EnvironmentName envName) {
        List<Study> studies = dao.findWithPreregContent(portalShortcode, envName);
        studies.forEach(this::attachStudyEnvironmentKitTypes);

        return studies;
    }

    public void attachStudyEnvironmentKitTypes(Study study) {
        study.getStudyEnvironments().forEach(studyEnv ->
                studyEnv.setKitTypes(studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(studyEnv.getId())));
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
                log.info("Deleting orphan study {}", studyId);
                delete(studyId, cascades);
            } else {
                log.info("Not deleting study {}, referenced in other portal", studyId);
            }
        });
    }

    public void attachEnvironments(Study study) {
        study.setStudyEnvironments(studyEnvironmentService.findByStudy(study.getId()));
    }
}
