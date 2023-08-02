package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.portal.PortalDao;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class BaseJdbiDaoTests extends BaseSpringBootTest {
    /** we use the portalDao to test base capability since it doesn't have any required foreign keys */
    @Autowired
    PortalDao portalDao;
    @Autowired
    PortalFactory portalFactory;

    @Test
    public void testGenerateInsertFields() {
        SimpleModelDao testDao = new SimpleModelDao(null);
        List<String> fields = testDao.insertFields;
        var expectedFields = new String[]{"createdAt", "lastUpdatedAt", "boolField",
                "intField", "doubleField", "stringField", "uuidField", "instantField"};
        assertThat(fields.toString(), fields, containsInAnyOrder(expectedFields));
    }

    @Getter @Setter
    private class SimpleModel extends BaseEntity {
        private boolean boolField;
        private int intField;
        private Double doubleField;
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

    @Test
    @Transactional
    public void testBulkInsertOfBasicList() {
        Portal portal1 = portalFactory.builder("testBulkInsertOfBasicList").build();
        Portal portal2 = portalFactory.builder("testBulkInsertOfBasicList").build();
        portalDao.bulkCreate(List.of(portal1, portal2));
        assertThat(portalDao.findOneByShortcode(portal1.getShortcode()).get().getName(), equalTo(portal1.getName()));
        assertThat(portalDao.findOneByShortcode(portal2.getShortcode()).get().getName(), equalTo(portal2.getName()));
    }

    @Test
    @Transactional
    public void testBulkInsertFailsIfAnyFail() {
        Portal portal1 = portalFactory.builder("testBulkInsertOfBasicList").build();
        Portal portal2 = portalFactory.builder("testBulkInsertOfBasicList").build();
        Portal portal3 = Portal.builder().shortcode(null).build();
        Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
            portalDao.bulkCreate(List.of(portal1, portal2, portal3));
        });
    }

    @Test
    @Transactional
    public void testStreamAllByProperty() {
        // Arrange
        var portal1 = portalDao.create(portalFactory.builder("").name("testStreamAllByProperty").build());
        var portal2 = portalDao.create(portalFactory.builder("").name("testStreamAllByProperty").build());

        // Act
        var stream = portalDao.streamAllByProperty("name", "testStreamAllByProperty");
        var foundPortals = stream.toList();

        // Assert
        assertThat(foundPortals, contains(portal1, portal2));
    }

    @Test
    @Transactional
    public void testStreamAllByPropertyCollection() {
        // Arrange
        var portal1 = portalDao.create(portalFactory.builder("").name("testStreamAllByProperty").build());
        var portal2 = portalDao.create(portalFactory.builder("").name("testStreamAllByProperty").build());
        var portalIds = List.of(portal1.getId(), portal2.getId());

        // Act
        var stream = portalDao.streamAllByPropertyCollection("id", portalIds);
        var foundPortals = stream.toList();

        // Assert
        assertThat(foundPortals, contains(portal1, portal2));
    }
}
