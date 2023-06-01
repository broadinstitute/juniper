package bio.terra.pearl.core.factory.kit;

import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.model.kit.KitType;
import org.springframework.stereotype.Component;

@Component
public class KitTypeFactory {
    private final KitTypeDao kitTypeDao;

    public KitTypeFactory(KitTypeDao kitTypeDao) {
        this.kitTypeDao = kitTypeDao;
    }

    public KitType.KitTypeBuilder builder(String testName) {
        return KitType.builder()
                .name(testName)
                .displayName(testName + " kit")
                .description("Kit type for " + testName);
    }

    public KitType buildPersisted(String testName) {
        var kitType = builder(testName).build();
        return kitTypeDao.create(kitType);
    }
}
