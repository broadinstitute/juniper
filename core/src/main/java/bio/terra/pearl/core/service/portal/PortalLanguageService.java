package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalLanguageDao;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PortalLanguageService extends ImmutableEntityService<PortalEnvironmentLanguage, PortalLanguageDao> {

    public PortalLanguageService(PortalLanguageDao portalLanguageDao) {
        super(portalLanguageDao);
    }

    public List<PortalEnvironmentLanguage> findByPortalEnvId(UUID portalId) {
        return dao.findByPortalEnvId(portalId);
    }

    /** for now, just do a hard delete/recreate */
    public List<PortalEnvironmentLanguage> setPortalEnvLanguages(UUID portalEnvId, List<PortalEnvironmentLanguage> languages) {
        dao.deleteByPortalEnvId(portalEnvId);
        return languages.stream().map(language -> {
            language.setPortalEnvironmentId(portalEnvId);
            return dao.create(language);
        }).toList();
    }

}
