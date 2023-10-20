package bio.terra.pearl.core.dao;

import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DaoUtils {
    private final Jdbi jdbi;
    public DaoUtils(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /**
     * gets a uuid from the database.
     * Useful for when you need an id but do not want to create an object just yet
     * */
    public UUID generateUUID() {
        return jdbi.withHandle(handle -> handle.createQuery("select gen_random_uuid ();")
                .mapTo(UUID.class)
                .one()
        );
    }
}
