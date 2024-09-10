package bio.terra.pearl.compliance.model;

import org.springframework.core.ParameterizedTypeReference;

import java.util.Set;

public class VantaIntegration<T extends VantaObject> {

    private final String integrationId;
    private final String resourceKind;
    private ParameterizedTypeReference<VantaResultsResponse<T>> resultsResponseType;
    private Set<String> resourceIdsToIgnore;


    public VantaIntegration(String integrationId, String resourceKind,
                            ParameterizedTypeReference<VantaResultsResponse<T>> resultsResponseType,
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

    public ParameterizedTypeReference<VantaResultsResponse<T>> getResultsResponseType() {
        return resultsResponseType;
    }

    public boolean shouldIgnoreResource(String resourceId) {
        return resourceIdsToIgnore.contains(resourceId);
    }
}
