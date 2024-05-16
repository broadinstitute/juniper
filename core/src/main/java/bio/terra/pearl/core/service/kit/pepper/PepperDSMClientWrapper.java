package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

/**
 * Wraps multiple PepperDsmClient implementations and delegates appropriately using the study environment config
 */

@Service
public class PepperDSMClientWrapper implements PepperDSMClient {
    private final LivePepperDSMClient.PepperDSMConfig pepperDSMConfig;
    private final LivePepperDSMClient livePepperDSMClient;
    private final StubPepperDSMClient stubPepperDSMClient;

    public PepperDSMClientWrapper(LivePepperDSMClient.PepperDSMConfig pepperDSMConfig,
                                  LivePepperDSMClient livePepperDSMClient,
                                  StubPepperDSMClient stubPepperDSMClient) {
        this.pepperDSMConfig = pepperDSMConfig;
        this.livePepperDSMClient = livePepperDSMClient;
        this.stubPepperDSMClient = stubPepperDSMClient;
    }

    /**
     * Resolves the PepperDSMClient implementation to use in the deployed application.
     */
    public PepperDSMClient getPepperDSMClient(StudyEnvironmentConfig studyEnvironmentConfig) {
        return studyEnvironmentConfig.isUseStubDsm() ? stubPepperDSMClient : livePepperDSMClient;
    }

    @Override
    public PepperKit sendKitRequest(String studyShortcode, StudyEnvironmentConfig studyEnvironmentConfig, Enrollee enrollee, KitRequest kitRequest, PepperKitAddress address) throws PepperApiException, PepperParseException {
        return getPepperDSMClient(studyEnvironmentConfig)
                .sendKitRequest(studyShortcode, studyEnvironmentConfig, enrollee, kitRequest, address);
    }

    @Override
    public PepperKit fetchKitStatus(StudyEnvironmentConfig studyEnvironmentConfig, UUID kitRequestId) throws PepperApiException, PepperParseException {
        return getPepperDSMClient(studyEnvironmentConfig).fetchKitStatus(studyEnvironmentConfig, kitRequestId);
    }

    @Override
    public Collection<PepperKit> fetchKitStatusByStudy(String studyShortcode, StudyEnvironmentConfig studyEnvironmentConfig) throws PepperApiException, PepperParseException {
        return getPepperDSMClient(studyEnvironmentConfig).fetchKitStatusByStudy(studyShortcode, studyEnvironmentConfig);
    }
}
