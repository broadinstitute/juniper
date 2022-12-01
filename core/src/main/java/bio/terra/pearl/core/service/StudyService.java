package bio.terra.pearl.core.service;

import bio.terra.pearl.core.dao.StudyDao;
import bio.terra.pearl.core.model.Study;
import bio.terra.pearl.core.model.StudyEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StudyService {
    private StudyDao studyDao;
    private StudyEnvironmentService studyEnvironmentService;

    public enum AllowedCascades implements CascadeProperty {
        STUDY_ENVIRONMENTS
    }

    public StudyService(StudyDao studyDao, StudyEnvironmentService studyEnvironmentService) {
        this.studyDao = studyDao;
        this.studyEnvironmentService = studyEnvironmentService;
    }

    @Transactional
    public Study create(Study study) {
        return studyDao.create(study);
    }

    @Transactional
    public Study create(Study study, CascadeTree cascade) {
        Study newStudy = create(study);
        if (cascade.hasProperty(AllowedCascades.STUDY_ENVIRONMENTS)) {
            study.getStudyEnvironments().forEach(studyEnv -> {
                studyEnv.setStudyId(newStudy.getId());
                StudyEnvironment newEnv = studyEnvironmentService
                        .create(studyEnv, cascade.getChild(AllowedCascades.STUDY_ENVIRONMENTS));
                newStudy.getStudyEnvironments().add(newEnv);
            });
        }
        return newStudy;
    }



}
