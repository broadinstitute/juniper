package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.kit.StudyEnvironmentKitTypeDao;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class StudyEnvironmentKitTypeService extends CrudService<StudyEnvironmentKitType, StudyEnvironmentKitTypeDao> {
    private final KitTypeDao kitTypeDao;

    public StudyEnvironmentKitTypeService(StudyEnvironmentKitTypeDao dao, KitTypeDao kitTypeDao) {
        super(dao);
        this.kitTypeDao = kitTypeDao;
    }

    public List<KitType> findKitTypesByStudyEnvironmentId(UUID studyEnvId) {
        List<StudyEnvironmentKitType> studyKitTypes = dao.findByStudyEnvironmentId(studyEnvId);
        return kitTypeDao.findAll(studyKitTypes.stream().map(StudyEnvironmentKitType::getKitTypeId).toList());
    }

    public List<KitType> findAllowedKitTypes() {
        return kitTypeDao.findAll();
    }

    public void deleteByKitTypeIdAndStudyEnvironmentId(UUID kitTypeId, UUID studyEnvId) {
        dao.deleteByKitTypeIdAndStudyEnvironmentId(kitTypeId, studyEnvId);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId, Set<CascadeProperty> cascades) {
        for (StudyEnvironmentKitType studyEnvironmentKitType : dao.findByStudyEnvironmentId(studyEnvId)) {
            dao.delete(studyEnvironmentKitType.getId());
        }
    }
}
