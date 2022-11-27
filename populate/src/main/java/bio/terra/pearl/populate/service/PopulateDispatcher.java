package bio.terra.pearl.populate.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PopulateDispatcher {
    private Map<PopulateType, Populator> typeMap;

    public PopulateDispatcher(PortalPopulator portalPopulator,
                              StudyPopulator studyPopulator,
                              EnvironmentPopulator environmentPopulator) {
        this.typeMap = Map.of(
                PopulateType.PORTAL, portalPopulator,
                PopulateType.STUDY, studyPopulator,
                PopulateType.ENVIRONMENT, environmentPopulator
        );
    }

    public Populator getPopulator(String populateType) {
        PopulateType popType = PopulateType.valueOf(populateType.toUpperCase());
        if (popType == null) {
            throw new IllegalArgumentException("'" + popType + "' does not match a known populator");
        }
        return typeMap.get(popType);
    }

    public enum PopulateType {
        PORTAL,
        STUDY,
        ENVIRONMENT
    }

}
