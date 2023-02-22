package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.populate.dao.ConsentFormPopulateDao;
import bio.terra.pearl.populate.dto.consent.ConsentFormPopDto;
import bio.terra.pearl.populate.dto.consent.StudyEnvironmentConsentPopDto;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/** populates ConsentForms.  this currently has a lot in common with SurveyPopulator */
@Service
public class ConsentFormPopulator extends Populator<ConsentForm> {
    private ConsentFormService consentFormService;
    private PortalService portalService;
    private ConsentFormPopulateDao consentFormPopulateDao;

    public ConsentFormPopulator(ConsentFormService consentFormService,
                                PortalService portalService, ConsentFormPopulateDao consentFormPopulateDao) {
        this.consentFormService = consentFormService;
        this.portalService = portalService;
        this.consentFormPopulateDao = consentFormPopulateDao;
    }

    @Override
    public ConsentForm populateFromString(String fileString, FilePopulateConfig config) throws IOException {
        ConsentFormPopDto consentPopDto = objectMapper.readValue(fileString, ConsentFormPopDto.class);
        String newContent = consentPopDto.getJsonContent().toString();
        consentPopDto.setContent(newContent);
        UUID portalId = portalService.findOneByShortcode(config.getPortalShortcode()).get().getId();
        consentPopDto.setPortalId(portalId);
        Optional<ConsentForm> existingOpt = fetchFromPopDto(consentPopDto);

        if (existingOpt.isPresent()) {
            ConsentForm existing = existingOpt.get();
            // don't delete the form, since it may have other entities attached to it. Just mod the content
            existing.setContent(consentPopDto.getContent());
            existing.setName(consentPopDto.getName());
            consentFormPopulateDao.update(existing);
            return existing;
        }
        return consentFormService.create(consentPopDto);
    }

    public Optional<ConsentForm> fetchFromPopDto(ConsentFormPopDto formDto) {
        return consentFormService.findByStableId(formDto.getStableId(),
                formDto.getVersion());
    }

    public StudyEnvironmentConsent convertConfiguredConsent(StudyEnvironmentConsentPopDto configConsentDto, int index) {
        StudyEnvironmentConsent configuredConsent = new StudyEnvironmentConsent();
        BeanUtils.copyProperties(configConsentDto, configuredConsent);
        ConsentForm consent = consentFormService.findByStableId(configConsentDto.getConsentStableId(),
                configConsentDto.getConsentVersion()).get();
        configuredConsent.setConsentFormId(consent.getId());
        configuredConsent.setConsentOrder(index);
        return configuredConsent;
    }
}
