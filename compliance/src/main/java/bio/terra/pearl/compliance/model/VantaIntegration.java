package bio.terra.pearl.compliance.model;

import org.springframework.core.ParameterizedTypeReference;

import java.util.Set;

public class VantaIntegration<T extends VantaObject> {
    private final String integrationId;
    private final String resourceKind;
    private ParameterizedTypeReference<? extends VantaResultsResponse<? extends VantaObject>> resultsResponseType;
    private Set<String> resourceIdsToIgnore;


    public <T extends VantaObject> VantaIntegration(String integrationId, String resourceKind,
                                                    ParameterizedTypeReference<? extends VantaResultsResponse<T>> resultsResponseType,
                                                    Set<String> resourceIdsToIgnore) {
        this.integrationId = integrationId;
        this.resourceKind = resourceKind;
        this.resultsResponseType = resultsResponseType;
        this.resourceIdsToIgnore = resourceIdsToIgnore;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public String getResourceKind() {
        return resourceKind;
    }

    public ParameterizedTypeReference<? extends VantaResultsResponse<? extends VantaObject>> getResultsResponseType() {
        return resultsResponseType;
    }

    public boolean shouldIgnoreResource(String resourceId) {
        return resourceIdsToIgnore.contains(resourceId);
    }
}
