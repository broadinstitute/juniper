package bio.terra.pearl.core.dao;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.portal.PortalDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.survey.Survey;
import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BaseJdbiDaoTests extends BaseSpringBootTest {
    /** we use the portalDao to test base capability since it doesn't have any required foreign keys */
    @Autowired
    PortalDao portalDao;
    @Autowired
    SurveyDao surveyDao;
    @Autowired
    PortalFactory portalFactory;
    @Autowired
    SurveyFactory surveyFactory;

    @Test
    public void testGenerateInsertFields() {
        SimpleModelDao testDao = new SimpleModelDao(null);
        List<String> fields = testDao.insertFields;
        // 'id','relatedStudy', and 'surveyList' should not be included
        String[] expectedFields = new String[]{"createdAt", "lastUpdatedAt", "boolField",
                "intField", "doubleField", "stringField", "uuidField", "instantField", "stringList"};
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
        private List<String> stringList;
        private List<Survey> surveyList;
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
    public void testFindAllByTwoPropertiesArray(TestInfo testInfo) {
        Survey surveyA1 = surveyFactory.buildPersisted(
                surveyFactory.builderWithDependencies(getTestName(testInfo)).stableId("A").version(1));
        Survey surveyA2 = surveyFactory.buildPersisted(
                surveyFactory.builderWithDependencies(getTestName(testInfo)).stableId("A").version(2));
        Survey surveyB1 = surveyFactory.buildPersisted(
                surveyFactory.builderWithDependencies(getTestName(testInfo)).stableId("B").version(1));
        Survey surveyB2 = surveyFactory.buildPersisted(
                surveyFactory.builderWithDependencies(getTestName(testInfo)).stableId("B").version(2));
        Survey surveyC1 = surveyFactory.buildPersisted(
                surveyFactory.builderWithDependencies(getTestName(testInfo)).stableId("C").version(1));

        List<Survey> surveys = surveyDao.findAllByTwoProperties(
                "stable_id", List.of("B", "A"), "version", List.of(1, 2));
        assertThat(surveys, contains(surveyB1, surveyA2));

        surveys = surveyDao.findAllByTwoProperties(
                "stable_id", List.of("C"), "version", List.of(2));
        assertThat(surveys, empty());

        surveys = surveyDao.findAllByTwoProperties(
                "stable_id", List.of(), "version", List.of());
        assertThat(surveys, empty());

        surveys = surveyDao.findAllByTwoProperties(
                "stable_id", List.of("B", "A", "B"), "version", List.of(2, 1, 2));
        assertThat(surveys, contains(surveyB2, surveyA1, surveyB2));
    }

    @Test
    @Transactional
    public void testBulkInsertOfBasicList(TestInfo testInfo) {
        Portal portal1 = portalFactory.builder(getTestName(testInfo)).build();
        Portal portal2 = portalFactory.builder(getTestName(testInfo)).build();
        portalDao.bulkCreate(List.of(portal1, portal2));
        assertThat(portalDao.findOneByShortcode(portal1.getShortcode()).get().getName(), equalTo(portal1.getName()));
        assertThat(portalDao.findOneByShortcode(portal2.getShortcode()).get().getName(), equalTo(portal2.getName()));
    }

    @Test
    @Transactional
    public void testBulkInsertFailsIfAnyFail(TestInfo testInfo) {
        Portal portal1 = portalFactory.builder(getTestName(testInfo)).build();
        Portal portal2 = portalFactory.builder(getTestName(testInfo)).build();
        Portal portal3 = Portal.builder().shortcode(null).build();
        Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
            portalDao.bulkCreate(List.of(portal1, portal2, portal3));
        });
    }

    @Test
    @Transactional
    public void testStreamAllByProperty(TestInfo testInfo) {
        // Arrange
        Portal portal1 = portalDao.create(portalFactory.builder("").name(getTestName(testInfo)).build());
        Portal portal2 = portalDao.create(portalFactory.builder("").name(getTestName(testInfo)).build());

        // Act
        Stream<Portal> stream = portalDao.streamAllByProperty("name", getTestName(testInfo));
        List<Portal> foundPortals = stream.toList();

        // Assert
        assertThat(foundPortals, contains(portal1, portal2));
    }

    @Test
    @Transactional
    public void testFindByPropertyCollectionPreserveOrder(TestInfo testInfo) {
        Portal portalA = portalDao.create(portalFactory.builder(getTestName(testInfo)).shortcode("A_Portal").build());
        Portal portalB = portalDao.create(portalFactory.builder(getTestName(testInfo)).shortcode("B_Portal").build());
        Portal portalC = portalDao.create(portalFactory.builder(getTestName(testInfo)).shortcode("C_Portal").build());

        List<String> shortcodeList = List.of("A_Portal","B_Portal","C_Portal");
        List<Portal> portals = portalDao.findAllByPropertyCollectionPreserveOrder("shortcode", shortcodeList);
        assertThat(portals.stream().map(Portal::getShortcode).toList(), equalTo(shortcodeList));

        shortcodeList = List.of("C_Portal","B_Portal", "A_Portal");
        portals = portalDao.findAllByPropertyCollectionPreserveOrder("shortcode", shortcodeList);
        assertThat(portals.stream().map(Portal::getShortcode).toList(), equalTo(shortcodeList));

        shortcodeList = List.of("B_Portal", "A_Portal");
        portals = portalDao.findAllByPropertyCollectionPreserveOrder("shortcode", shortcodeList);
        assertThat(portals.stream().map(Portal::getShortcode).toList(), equalTo(shortcodeList));
    }

    @Test
    @Transactional
    public void testFindAllByProperty(TestInfo testInfo) {
        Portal portal1 = portalDao.create(portalFactory.builder(getTestName(testInfo)).name("FOO").build());
        Portal portal2 = portalDao.create(portalFactory.builder(getTestName(testInfo)).name("FOO").build());
        Portal portal3 = portalDao.create(portalFactory.builder(getTestName(testInfo)).name("BAR").build());

        List<Portal> portals = portalDao.findAllByProperty("name", "FOO");
        assertThat(portals, contains(portal1, portal2));
        assertThat(portals, not(contains(portal3)));
    }

    @Test
    @Transactional
    public void testFindAllByPropertyNullSafe(TestInfo testInfo) {
        Portal portal = portalDao.create(portalFactory.builder(getTestName(testInfo)).name("FOO").build());
        Survey survey1 = surveyDao.create(surveyFactory.builder(getTestName(testInfo)).name("Survey 1").blurb("outreach1").portalId(portal.getId()).build());
        Survey survey2 = surveyDao.create(surveyFactory.builder(getTestName(testInfo)).name("Survey 2").blurb("outreach2").portalId(portal.getId()).build());
        Survey survey3 = surveyDao.create(surveyFactory.builder(getTestName(testInfo)).name("Survey 3").blurb(null).portalId(portal.getId()).build());
        Survey survey4 = surveyDao.create(surveyFactory.builder(getTestName(testInfo)).name("Survey 4").blurb(null).portalId(portal.getId()).build());

        List<Survey> surveys = surveyDao.findAllByProperty("blurb", null);

        assertThat(surveys, contains(survey3, survey4));
        assertThat(surveys, not(contains(survey1, survey2)));
    }
}
