package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.kit.StudyKitTypeDao;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyKitType;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class StudyKitTypeService extends CrudService<StudyKitType, StudyKitTypeDao> {
    private final KitTypeDao kitTypeDao;

    public StudyKitTypeService(StudyKitTypeDao dao, KitTypeDao kitTypeDao) {
        super(dao);
        this.kitTypeDao = kitTypeDao;
    }

    public List<KitType> findKitTypesByStudyId(UUID studyId) {
        var studyKitTypes = dao.findByStudyId(studyId);
        return kitTypeDao.findAll(studyKitTypes.stream().map(StudyKitType::getKitTypeId).toList());
    }

    public void deleteByStudyId(UUID studyId, Set<CascadeProperty> cascades) {
        for (StudyKitType studyKitType : dao.findByStudyId(studyId)) {
            dao.delete(studyKitType.getId());
        }
    }
}
