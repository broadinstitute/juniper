package bio.terra.pearl.populate.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PopulateDispatcher {
    private Map<PopulateType, Populator> typeMap;

    public enum PopulateType {
        ADMIN_USER,
        ENVIRONMENT,
        PORTAL,
        STUDY
    }

    public PopulateDispatcher(AdminUserPopulator adminUserPopulator,
                              EnvironmentPopulator environmentPopulator,
                              PortalPopulator portalPopulator,
                              StudyPopulator studyPopulator) {
        this.typeMap = Map.of(
                PopulateType.ADMIN_USER, adminUserPopulator,
                PopulateType.ENVIRONMENT, environmentPopulator,
                PopulateType.PORTAL, portalPopulator,
                PopulateType.STUDY, studyPopulator
        );
    }

    public Populator getPopulator(String populateType) {
        PopulateType popType = PopulateType.valueOf(populateType.toUpperCase());
        if (popType == null) {
            throw new IllegalArgumentException("'" + popType + "' does not match a known populator");
        }
        return typeMap.get(popType);
    }

}
