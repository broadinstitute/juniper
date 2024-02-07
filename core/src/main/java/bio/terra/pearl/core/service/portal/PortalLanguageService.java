package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalLanguageDao;
import bio.terra.pearl.core.model.portal.PortalLanguage;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PortalLanguageService extends ImmutableEntityService<PortalLanguage, PortalLanguageDao> {

    public PortalLanguageService(PortalLanguageDao portalLanguageDao) {
        super(portalLanguageDao);
    }

    public List<PortalLanguage> findByPortalId(UUID portalId) {
        return dao.findByPortalId(portalId);
    }

}
