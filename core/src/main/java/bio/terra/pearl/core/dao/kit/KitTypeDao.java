package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.kit.KitType;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KitTypeDao extends BaseMutableJdbiDao<KitType> {
    public KitTypeDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<KitType> getClazz() {
        return KitType.class;
    }

    public Optional<KitType> findByName(String name) {
        return findByProperty("name", name);
    }
}
