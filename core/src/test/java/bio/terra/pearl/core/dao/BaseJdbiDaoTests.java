package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.study.Study;
import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BaseJdbiDaoTests {
    @Test
    public void testGenerateInsertFields() {
        SimpleModelDao testDao = new SimpleModelDao(null);
        List<String> fields = testDao.insertFields;
        List<String> expectedFields = Arrays.asList("createdAt", "lastUpdatedAt", "boolField",
                "intField", "stringField", "uuidField", "instantField");
        Assertions.assertTrue(fields.size() == expectedFields.size() &&
                expectedFields.containsAll(fields) &&
                fields.containsAll(expectedFields));
    }

    @Getter @Setter
    private class SimpleModel extends BaseEntity {
        private boolean boolField;
        private int intField;
        private String stringField;
        private UUID uuidField;
        private Instant instantField;
        private Study relatedStudy;
    }

    private class SimpleModelDao extends BaseJdbiDao<SimpleModel> {
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
