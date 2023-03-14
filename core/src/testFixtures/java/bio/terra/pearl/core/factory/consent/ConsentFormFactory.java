package bio.terra.pearl.core.factory.consent;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsentFormFactory {
    @Autowired
    private ConsentFormService consentFormService;

    public ConsentForm.ConsentFormBuilder builder(String testName) {
        String randString = RandomStringUtils.randomAlphabetic(3);
        return ConsentForm.builder().version(1)
                .stableId(testName + "_" + randString)
                .name("Name " + randString + " consent form");
    }

    public ConsentForm.ConsentFormBuilder builderWithDependencies(String testName) {
        return builder(testName);
    }

    public ConsentForm buildPersisted(String testName) {
        return consentFormService.create(builderWithDependencies(testName).build());
    }
}
