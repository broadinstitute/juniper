package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CascadeTree;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudyService {
    private StudyDao studyDao;
    private StudyEnvironmentService studyEnvironmentService;
    private PortalStudyService portalStudyService;

    public enum AllowedCascades implements CascadeProperty {
        STUDY_ENVIRONMENTS
    }

    public StudyService(StudyDao studyDao, StudyEnvironmentService studyEnvironmentService,
                        PortalStudyService portalStudyService) {
        this.studyDao = studyDao;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalStudyService = portalStudyService;
    }

    public Optional<Study> findByShortcode(String shortcode) {
        return studyDao.findOneByShortcode(shortcode);
    }

    @Transactional
    public Study create(Study study) {
        Study newStudy = studyDao.create(study);
        study.getStudyEnvironments().forEach(studyEnv -> {
            studyEnv.setStudyId(newStudy.getId());
            StudyEnvironment newEnv = studyEnvironmentService.create(studyEnv);
            newStudy.getStudyEnvironments().add(newEnv);
        });
        return newStudy;
    }

    @Transactional
    public void delete(UUID studyId, CascadeTree cascades) {
        studyEnvironmentService.deleteByStudyId(studyId, cascades);
        studyDao.delete(studyId);
    }

    @Transactional
    public void deleteOrphans(List<UUID> studyIds, CascadeTree cascades) {
        studyIds.stream().forEach(studyId -> {
            if (portalStudyService.findByStudyId(studyId).size() == 0) {
                delete(studyId, cascades);
            }
        });
    }



}
