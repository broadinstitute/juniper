package bio.terra.pearl.core.factory.kit;

import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KitTypeFactory {
    @Autowired
    private KitTypeDao kitTypeDao;
    @Autowired
    private StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;

    public KitType.KitTypeBuilder builder(String testName) {
        return KitType.builder()
                .name(testName)
                .displayName(testName + " kit")
                .description("Kit type for " + testName);
    }

    public KitType buildPersisted(String testName) {
        KitType kitType = builder(testName).build();
        return kitTypeDao.create(kitType);
    }

    public void attachTypeToEnvironment(UUID kitTypeId, UUID studyEnvId) {
        StudyEnvironmentKitType studyEnvironmentKitType = StudyEnvironmentKitType.builder()
                .studyEnvironmentId(studyEnvId)
                .kitTypeId(kitTypeId)
                .build();
        studyEnvironmentKitTypeService.create(studyEnvironmentKitType);
    }
}
