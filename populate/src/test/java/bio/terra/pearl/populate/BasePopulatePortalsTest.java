package bio.terra.pearl.populate;

import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.service.export.DictionaryExportService;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import bio.terra.pearl.populate.service.EnvironmentPopulator;
import bio.terra.pearl.populate.service.PortalPopulator;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Attempts to populate every portal in the seed path.
 * Since the aim of the seed path is to have portals sufficient for a developer/demo-er to easily view
 * all aspects of product functionality.  So this test essentially confirms that a fresh development/demo
 * environment can be created with all functionality accessible.
 */
public abstract class BasePopulatePortalsTest extends BaseSpringBootTest {
    @Autowired
    protected PortalPopulator portalPopulator;
    @Autowired
    protected EnvironmentPopulator environmentPopulator;
    @Autowired
    protected StudyEnvironmentService studyEnvironmentService;
    @Autowired
    protected EnrolleeService enrolleeService;
    @Autowired
    protected SurveyService surveyService;
    @Autowired
    protected SurveyResponseService surveyResponseService;
    @Autowired
    protected PortalEnvironmentService portalEnvironmentService;
    @Autowired
    protected EnrolleeExportService enrolleeExportService;
    @Autowired
    protected EnrolleeFactory enrolleeFactory;
    @Autowired
    protected DictionaryExportService dictionaryExportService;

    protected void setUpEnvironments() throws IOException {
        for (String fileName : BaseSeedPopulator.ENVIRONMENTS_TO_POPULATE) {
            environmentPopulator.populate(new FilePopulateContext(fileName), true);
        }
    }
}
