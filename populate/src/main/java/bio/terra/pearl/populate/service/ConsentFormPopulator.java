package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.populate.dao.ConsentFormPopulateDao;
import bio.terra.pearl.populate.dto.consent.ConsentFormPopDto;
import bio.terra.pearl.populate.dto.consent.StudyEnvironmentConsentPopDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/** populates ConsentForms.  this currently has a lot in common with SurveyPopulator */
@Service
public class ConsentFormPopulator extends BasePopulator<ConsentForm, ConsentFormPopDto, PortalPopulateContext> {
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
    protected void preProcessDto(ConsentFormPopDto popDto, PortalPopulateContext context) {
        UUID portalId = portalService.findOneByShortcode(context.getPortalShortcode()).get().getId();
        popDto.setPortalId(portalId);
        String newContent = popDto.getJsonContent().toString();
        popDto.setContent(newContent);
    }

    public StudyEnvironmentConsent convertConfiguredConsent(StudyEnvironmentConsentPopDto configConsentDto,
                                                            int index, FilePopulateContext context) {
        StudyEnvironmentConsent configuredConsent = new StudyEnvironmentConsent();
        BeanUtils.copyProperties(configConsentDto, configuredConsent);
        ConsentForm consent;
        if (configConsentDto.getPopulateFileName() != null) {
            consent = context.fetchFromPopDto(configConsentDto, consentFormService).get();
        } else {
            consent = consentFormService.findByStableId(configConsentDto.getConsentStableId(),
                    configConsentDto.getConsentVersion()).get();
        }
        configuredConsent.setConsentFormId(consent.getId());
        configuredConsent.setConsentOrder(index);
        configuredConsent.setConsentForm(consent);
        return configuredConsent;
    }

    @Override
    protected Class<ConsentFormPopDto> getDtoClazz() {
        return ConsentFormPopDto.class;
    }

    @Override
    public ConsentForm createNew(ConsentFormPopDto popDto, PortalPopulateContext context, boolean overwrite) {
        return consentFormService.create(popDto);
    }

    @Override
    public ConsentForm createPreserveExisting(ConsentForm existingObj, ConsentFormPopDto popDto, PortalPopulateContext context) {
        if (Objects.equals(existingObj.getContent(), popDto.getContent())) {
            // the things are the same, don't bother creating a new version
            return existingObj;
        }
        int newVersion = consentFormService.getNextVersion(popDto.getStableId());
        popDto.setVersion(newVersion);
        return consentFormService.create(popDto);
    }

    @Override
    public ConsentForm overwriteExisting(ConsentForm existingObj, ConsentFormPopDto popDto, PortalPopulateContext context) {
        // don't delete the form, since it may have other entities attached to it. Just mod the content
        existingObj.setContent(popDto.getContent());
        existingObj.setName(popDto.getName());
        return consentFormPopulateDao.update(existingObj);
    }

    @Override
    public Optional<ConsentForm> findFromDto(ConsentFormPopDto popDto, PortalPopulateContext context) {
        Optional<ConsentForm> existingOpt = context.fetchFromPopDto(popDto, consentFormService);
        if (existingOpt.isPresent()) {
            return existingOpt;
        }
        return consentFormService.findByStableId(popDto.getStableId(), popDto.getVersion());
    }
}
