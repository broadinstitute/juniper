package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.model.EnvironmentName;

public class EnvironmentFactory {
    public static Environment.EnvironmentBuilder builder() {
        return Environment.builder().name(EnvironmentName.sandbox);
    }

    public static Environment buildPersisted(DaoHolder daoHolder) {
        return buildPersisted(daoHolder, builder());
    }

    public static Environment buildPersisted(DaoHolder daoHolder, Environment.EnvironmentBuilder builder) {
        return daoHolder.environmentDao.createOrUpdate(builder.build());
    }
}
