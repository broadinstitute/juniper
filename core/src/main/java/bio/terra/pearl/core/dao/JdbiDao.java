package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public interface JdbiDao<T extends BaseEntity> {
    BaseJdbiDao<T> getDao();
}
