package bio.terra.pearl.core.service.kit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PepperDSMClientProvider {
    private final LivePepperDSMClient.PepperDSMConfig pepperDSMConfig;
    private final LivePepperDSMClient livePepperDSMClient;
    private final StubPepperDSMClient stubPepperDSMClient;

    public PepperDSMClientProvider(LivePepperDSMClient.PepperDSMConfig pepperDSMConfig,
                                   LivePepperDSMClient livePepperDSMClient,
                                   StubPepperDSMClient stubPepperDSMClient) {
        this.pepperDSMConfig = pepperDSMConfig;
        this.livePepperDSMClient = livePepperDSMClient;
        this.stubPepperDSMClient = stubPepperDSMClient;
    }

    /**
     * Resolves the PepperDSMClient implementation to use in the deployed application.
     */
    @Primary
    @Bean
    public PepperDSMClient getPepperDSMClient() {
        return pepperDSMConfig.useLiveDsm() ?
                getLivePepperDSMClient() :
                getStubPepperDSMClient();
    }

    @Bean
    public LivePepperDSMClient getLivePepperDSMClient() {
        return livePepperDSMClient;
    }

    @Bean
    public StubPepperDSMClient getStubPepperDSMClient() {
        return stubPepperDSMClient;
    }
}
