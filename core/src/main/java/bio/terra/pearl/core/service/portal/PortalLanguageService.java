package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalLanguageDao;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PortalLanguageService extends ImmutableEntityService<PortalEnvironmentLanguage, PortalLanguageDao> {

    public PortalLanguageService(PortalLanguageDao portalLanguageDao) {
        super(portalLanguageDao);
    }

    public List<PortalEnvironmentLanguage> findByPortalId(UUID portalId) {
        return dao.findByPortalEnvId(portalId);
    }

}
