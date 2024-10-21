package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.study.Study;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BaseMutableJdbiTests {

    @Test
    public void testGenerateUpdateFieldString() {
        BaseMutableJdbiTests.SimpleModelDao testDao = new BaseMutableJdbiTests.SimpleModelDao(null);
        // notably, this should NOT contain 'created_at'
        Assertions.assertEquals("bool_field = :boolField, int_field = :intField, string_field = :stringField, uuid_field = :uuidField, instant_field = :instantField, last_updated_at = :lastUpdatedAt",
                testDao.updateFieldString);
    }

    @Test
    public void testUpsertSql() {
        BaseMutableJdbiTests.SimpleModelDao testDao = new BaseMutableJdbiTests.SimpleModelDao(null);
        Assertions.assertEquals("insert into simple_model (bool_field, int_field, string_field, uuid_field, instant_field, created_at, last_updated_at) " +
                        "values (:boolField, :intField, :stringField, :uuidField, :instantField, :createdAt, :lastUpdatedAt) " +
                        "on conflict (uuid_field) do update set " +
                        "bool_field = excluded.bool_field, int_field = excluded.int_field, string_field = excluded.string_field, " +
                        "uuid_field = excluded.uuid_field, instant_field = excluded.instant_field, last_updated_at = excluded.last_updated_at",
                testDao.getUpsertQuerySql("uuid_field"));
    }

    @Getter
    @Setter
    private class SimpleModel extends BaseEntity {
        private boolean boolField;
        private int intField;
        private String stringField;
        private UUID uuidField;
        private Instant instantField;
        private Study relatedStudy;
    }

    class SimpleModelDao extends BaseMutableJdbiDao<SimpleModel> {
        public SimpleModelDao(Jdbi jdbi) {
            super(jdbi);
        }
        @Override
        protected void initializeRowMapper(Jdbi jdbi) {
            // do nothing, since this isn't working with a real jdbi instance
        }

        @Override
        protected Class<SimpleModel> getClazz() {
            return SimpleModel.class;
        }
    }
}
