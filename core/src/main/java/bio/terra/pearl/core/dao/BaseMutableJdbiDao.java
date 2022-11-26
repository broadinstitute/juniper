package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.BeanUtils;

public abstract class BaseMutableJdbiDao<T extends BaseEntity> extends BaseJdbiDao<T> {
    public BaseMutableJdbiDao(Jdbi jdbi) {
        super(jdbi);
    }
    public T createOrUpdate(T matchObj) {
        T existingObj = findOneMatch(matchObj).get();
        if (existingObj == null) {
            return create(matchObj);
        }
        BeanUtils.copyProperties(matchObj, existingObj, new String[] {"id"});
        return update(existingObj);
    }

    public T update(T matchObj) {
        if (matchObj.getId() == null) {
            throw new RuntimeException("attempted update on " + clazz + " with no id");
        }
        return jdbi.withHandle(handle ->
                handle.createUpdate("update " + tableName + " set " + updateFieldString +
                                " where id = :id;")
                        .bindBean(matchObj)
                        .executeAndReturnGeneratedKeys()
                        .mapTo(clazz)
                        .one()
        );
    }
}
