package bio.terra.pearl.compliance.model;

import java.util.Set;

public class VantaIntegration<T extends VantaObject> {
    private final String integrationId;
    private final String resourceKind;
    private Class<? extends VantaResultsResponse<? extends VantaObject>> resultsResponseClass;
    private Set<String> resourceIdsToIgnore;


    public <T extends VantaObject> VantaIntegration(String integrationId, String resourceKind,
                                                    Class<? extends VantaResultsResponse<T>> resultsResponseClass,
                                                    Set<String> resourceIdsToIgnore) {
        this.integrationId = integrationId;
        this.resourceKind = resourceKind;
        this.resultsResponseClass = resultsResponseClass;
        this.resourceIdsToIgnore = resourceIdsToIgnore;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public String getResourceKind() {
        return resourceKind;
    }

    public Class<? extends VantaResultsResponse<? extends VantaObject>> getResultsResponseClass() {
        return resultsResponseClass;
    }

    public boolean shouldIgnoreResource(String resourceId) {
        return resourceIdsToIgnore.contains(resourceId);
    }
}
