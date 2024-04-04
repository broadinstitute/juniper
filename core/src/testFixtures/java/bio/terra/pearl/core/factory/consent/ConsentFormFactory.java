package bio.terra.pearl.core.factory.consent;

import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConsentFormFactory {
    @Autowired
    private ConsentFormService consentFormService;
    @Autowired
    private StudyEnvironmentConsentService studyEnvironmentConsentService;
    @Autowired
    private PortalFactory portalFactory;

    public ConsentForm.ConsentFormBuilder builder(String testName) {
        String randString = RandomStringUtils.randomAlphabetic(3);
        return ConsentForm.builder().version(1)
                .stableId(testName + "_" + randString)
                .name("Name " + randString + " consent form");
    }

    public ConsentForm.ConsentFormBuilder builderWithDependencies(String testName) {
        Portal portal = portalFactory.buildPersisted(testName);
        return builder(testName).portalId(portal.getId());
    }

    public ConsentForm buildPersisted(ConsentForm.ConsentFormBuilder builder) {
        return consentFormService.create(builder.build());
    }

    public ConsentForm buildPersisted(String testName) {
        return consentFormService.create(builderWithDependencies(testName).build());
    }

    public ConsentForm buildPersisted(String testName, UUID portalId) {
        return consentFormService.create(builderWithDependencies(testName).portalId(portalId).build());
    }

    public StudyEnvironmentConsent addConsentToStudyEnv(UUID studyEnvId, UUID consentFormId) {
        StudyEnvironmentConsent consentConfig = StudyEnvironmentConsent.builder()
                .consentFormId(consentFormId)
                .studyEnvironmentId(studyEnvId)
                .build();
        return studyEnvironmentConsentService.create(consentConfig);
    }
}
