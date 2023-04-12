package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.BaseEntity;
import java.time.Instant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DaoTestUtils {
    /**
     * checks that an id got assigned, and the dates got updated to values within the past 2 seconds
     * */
    public static void assertGeneratedProperties(BaseEntity entity) {
        assertThat(entity.getId(), notNullValue());
        assertThat(entity.getCreatedAt(), lessThan(Instant.now()));
        assertThat(entity.getCreatedAt(), greaterThan(Instant.now().minusMillis(2000)));
        assertThat(entity.getLastUpdatedAt(), lessThan(Instant.now()));
        assertThat(entity.getLastUpdatedAt(), greaterThan(Instant.now().minusMillis(2000)));
    }
}
