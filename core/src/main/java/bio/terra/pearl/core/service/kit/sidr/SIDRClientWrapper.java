package bio.terra.pearl.core.service.kit.sidr;

import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.kit.sidr.models.SIDROrder;
import org.springframework.stereotype.Service;

/**
 * Wraps the live and stub SIDRClient implementations and delegates appropriately using the study environment config
 */
@Service
public class SIDRClientWrapper implements SIDRClient {
    private final LiveSIDRClient liveSIDRClient;
    private final StubSIDRClient stubSIDRClient;

    public SIDRClientWrapper(LiveSIDRClient liveSIDRClient, StubSIDRClient stubSIDRClient) {
        this.liveSIDRClient = liveSIDRClient;
        this.stubSIDRClient = stubSIDRClient;
    }

    /** Resolves the SIDRClient implementation to use for the given study environment */
    public SIDRClient getSIDRClient(StudyEnvironmentConfig config) {
        return config.isUseStubSidr() ? stubSIDRClient : liveSIDRClient;
    }

    @Override
    public SIDROrder postOrder(StudyEnvironmentConfig config, AdminUser actor, SIDROrder order) throws SIDRApiException {
        return getSIDRClient(config).postOrder(config, actor, order);
    }

    @Override
    public SIDROrder getOrder(StudyEnvironmentConfig config, String sidrOrderId) throws SIDRApiException {
        return getSIDRClient(config).getOrder(config, sidrOrderId);
    }
}
