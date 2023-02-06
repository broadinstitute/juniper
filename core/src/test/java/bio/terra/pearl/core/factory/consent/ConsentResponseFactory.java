package bio.terra.pearl.core.factory.consent;

import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.ConsentResponse;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.consent.ConsentResponseService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsentResponseFactory {
    @Autowired
    private ConsentResponseService consentResponseService;
    @Autowired
    private ConsentFormFactory consentFormFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;

    public ConsentResponse.ConsentResponseBuilder builder(String testName) {
        String randString = "{foo: " + RandomStringUtils.randomNumeric(3) + "}";
        return ConsentResponse.builder()
                .fullData(randString);
    }

    public ConsentResponse.ConsentResponseBuilder builderWithDependencies(String testName) {
        ConsentForm form = consentFormFactory.buildPersisted(testName);
        Enrollee enrollee = enrolleeFactory.buildPersisted(testName);
        return builder(testName)
                .consentFormId(form.getId())
                .enrolleeId(enrollee.getId());
    }

    public ConsentResponse buildPersisted(String testName) {
        return consentResponseService.create(builderWithDependencies(testName).build());
    }
}
