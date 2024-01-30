package bio.terra.pearl.populate;

import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import bio.terra.pearl.populate.service.SurveyPopulator;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SurveyPopulatorTests extends BaseSpringBootTest {
    @Autowired
    SurveyPopulator surveyPopulator;
    @Autowired
    private Jdbi jdbi;
    @Autowired
    SurveyService surveyService;
    @Autowired
    SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    @Autowired
    SurveyFactory surveyFactory;
    @Autowired
    PortalFactory portalFactory;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Transactional
    public void testPopulateClean() throws IOException {
        Portal portal = portalFactory.buildPersisted("testPopulateClean");
        String surveyFile = "portals/ourhealth/studies/ourheart/surveys/basic.json";
        PortalPopulateContext context =
                new PortalPopulateContext(surveyFile, portal.getShortcode(), null, new HashMap<>(), false);
        Survey freshSurvey = surveyPopulator.populate(context, false);
        checkSurvey(freshSurvey, "oh_oh_basicInfo", 1);

        Survey fetchedSurvey = surveyService.findByStableIdWithMappings("oh_oh_basicInfo", 1).get();
        // check that answer mappings populate too
        assertThat(fetchedSurvey.getAnswerMappings().size(), greaterThan(0));

        List<SurveyQuestionDefinition> questionDefs = surveyQuestionDefinitionDao.findAllBySurveyId(fetchedSurvey.getId());
        assertThat(questionDefs, hasSize(53));
    }

    @Test
    @Transactional
    public void testPopulateOverride() throws IOException {
        Portal portal = portalFactory.buildPersisted("testPopulateClean");
        String stableId = "testPopOver-" + RandomStringUtils.randomAlphabetic(5);
        SurveyPopDto popDto1 = SurveyPopDto.builder()
                .stableId(stableId)
                .version(1)
                .jsonContent(objectMapper.readTree("{\"foo\": 12}"))
                .name("Survey 1").build();
        PortalPopulateContext context =
                new PortalPopulateContext("fake/file", portal.getShortcode(), null, new HashMap<>(), false);
        Survey newSurvey = surveyPopulator.populateFromDto(popDto1, context, true);
        checkSurvey(newSurvey, stableId, 1);

        SurveyPopDto popDto2 = SurveyPopDto.builder()
                .stableId(stableId)
                .version(1)
                .jsonContent(objectMapper.readTree("{\"foo\":17}"))
                .name("Survey 1").build();
        PortalPopulateContext context2 =
                new PortalPopulateContext("fake/file", portal.getShortcode(), null, new HashMap<>(), false);
        Survey overrideSurvey = surveyPopulator.populateFromDto(popDto2, context, true);
        // should override the previous survey, and so still be version 1
        checkSurvey(overrideSurvey, stableId, 1);
        Survey loadedSurvey = surveyService.findByStableId(stableId, 1).get();
        assertThat(loadedSurvey.getContent(), equalTo("{\"foo\":17}"));
    }

    @Test
    @Transactional
    public void testPopulateNoOverride() throws IOException {
        Portal portal = portalFactory.buildPersisted("testPopulateNoOverride");
        String stableId = "testPopNoOver-" + RandomStringUtils.randomAlphabetic(5);
        SurveyPopDto popDto1 = SurveyPopDto.builder()
                .stableId(stableId)
                .version(1)
                .jsonContent(objectMapper.readTree("{\"foo\": 12}"))
                .name("Survey 1").build();
        PortalPopulateContext context =
                new PortalPopulateContext("fake/file", portal.getShortcode(), null, new HashMap<>(), false);
        Survey newSurvey = surveyPopulator.populateFromDto(popDto1, context, true);
        checkSurvey(newSurvey, stableId, 1);

        SurveyPopDto popDto2 = SurveyPopDto.builder()
                .stableId(stableId)
                .version(1)
                .jsonContent(objectMapper.readTree("{\"foo\":17}"))
                .name("Survey 1").build();
        PortalPopulateContext context2 =
                new PortalPopulateContext("fake/file", portal.getShortcode(), null, new HashMap<>(), false);
        Survey overrideSurvey = surveyPopulator.populateFromDto(popDto2, context, false);

        // should NOT override the previous survey, and so still be saved as version 2
        checkSurvey(overrideSurvey, stableId, 2);
        Survey loadedSurvey = surveyService.findByStableId(stableId, 2).get();
        assertThat(loadedSurvey.getContent(), equalTo("{\"foo\":17}"));

        // prior survey should have no updates
        Survey loadedPrevSurvey = surveyService.findByStableId(stableId, 1).get();
        assertThat(loadedPrevSurvey.getContent(), equalTo("{\"foo\":12}"));
    }

    private void checkSurvey(Survey survey, String expectedStabledId, int expectedVersion) {
        DaoTestUtils.assertGeneratedProperties(survey);
        assertThat(survey.getStableId(), equalTo(expectedStabledId));
        assertThat(survey.getVersion(), equalTo(expectedVersion));
    }


}
