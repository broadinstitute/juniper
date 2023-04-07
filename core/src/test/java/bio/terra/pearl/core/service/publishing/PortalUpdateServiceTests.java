package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.service.notification.EmailTemplateService;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.springframework.beans.factory.annotation.Autowired;

public class PortalUpdateServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalDiffService portalDiffService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testApplyConfigChanges() throws Exception {
        ConfigChange change = new ConfigChange("password", (Object) "foo", (Object)"bar");
        PortalEnvironmentConfig config = PortalEnvironmentConfig.builder()
                .password("foo").build();
        PortalEnvironment portalEnvironment = PortalEnvironment.builder()
                        .portalEnvironmentConfig(config).build();
        NonPersistentPortalUpdateService npPortalUpdateService = NonPersistentPortalUpdateService
                .newInstance(portalDiffService, objectMapper);
        npPortalUpdateService.applyChangesToEnvConfig(portalEnvironment, List.of(change));
        assertThat(config.getPassword(), equalTo("bar"));
    }

    public static class NonPersistentPortalUpdateService extends PortalUpdateService {
        protected NonPersistentPortalUpdateService(PortalDiffService portalDiffService, PortalEnvironmentService portalEnvironmentService,
                                                   PortalEnvironmentConfigService portalEnvironmentConfigService,
                                                   PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao,
                                                   NotificationConfigService notificationConfigService, SurveyService surveyService,
                                                   EmailTemplateService emailTemplateService, SiteContentService siteContentService,
                                                   StudyUpdateService studyUpdateService, ObjectMapper objectMapper) {
            super(portalDiffService, portalEnvironmentService, portalEnvironmentConfigService,
                    portalEnvironmentChangeRecordDao, notificationConfigService,
                    surveyService, emailTemplateService, siteContentService, studyUpdateService, objectMapper);


        }

        /** we use a static constructor so the mocks can be initialized in the running test context */
        public static NonPersistentPortalUpdateService newInstance(PortalDiffService portalDiffService, ObjectMapper objectMapper) {
            PortalEnvironmentService mockPortalEnvService = mock(PortalEnvironmentService.class);
            PortalEnvironmentConfigService mockPortalEnvConfigService = mock(PortalEnvironmentConfigService.class);
            PortalEnvironmentChangeRecordDao mockPortalEnvChangeRecordDao = mock(PortalEnvironmentChangeRecordDao.class);
            NotificationConfigService mockNotifConfigService = mock(NotificationConfigService.class);
            SurveyService mockSurvService = mock(SurveyService.class);
            EmailTemplateService mockEmailTempService = mock(EmailTemplateService.class);
            SiteContentService mockSiteConService = mock(SiteContentService.class);
            StudyUpdateService mockStudyUpService = mock(StudyUpdateService.class);
            return new NonPersistentPortalUpdateService(portalDiffService, mockPortalEnvService, mockPortalEnvConfigService,
                    mockPortalEnvChangeRecordDao, mockNotifConfigService, mockSurvService,
                    mockEmailTempService, mockSiteConService, mockStudyUpService, objectMapper);
        }


    }





}
