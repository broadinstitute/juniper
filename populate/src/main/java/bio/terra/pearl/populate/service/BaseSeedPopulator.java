package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.service.EnvironmentService;
import bio.terra.pearl.core.service.admin.AdminUserService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Service;

/**
 * Populates entities configs needed for a new server instance, including environments and admin users.
 * If those users/items exist, the will be updated, rather than duplicated.
 * */
@Service
public class BaseSeedPopulator extends Populator<BaseSeedPopulator.SetupStats> {
    private AdminUserPopulator adminUserPopulator;
    private EnvironmentPopulator environmentPopulator;
    private AdminUserService adminUserService;
    private EnvironmentService environmentService;

    private static final List<String> ADMIN_USERS_TO_POPULATE =
            Arrays.asList("adminUsers/dbush.json", "adminUsers/breilly.json",
                    "adminUsers/myanaman.json", "adminUsers/kkaratza.json",
                    "adminUsers/jkorte.json", "adminUsers/egwozdz.json",
                    "adminUsers/mflinn.json", "adminUsers/nwatts.json");
    private static final List<String> ENVIRONMENTS_TO_POPULATE =
            Arrays.asList("environments/sandbox.json", "environments/irb.json", "environments/live.json");

    public BaseSeedPopulator(AdminUserPopulator adminUserPopulator, EnvironmentPopulator environmentPopulator,
                             AdminUserService adminUserService, EnvironmentService environmentService) {
        this.adminUserPopulator = adminUserPopulator;
        this.environmentPopulator = environmentPopulator;
        this.adminUserService = adminUserService;
        this.environmentService = environmentService;
    }

    public SetupStats populate(String filePathName) throws IOException {
        // for now, we ignore the pathname
        for (String file : ADMIN_USERS_TO_POPULATE) {
            adminUserPopulator.populate(file);
        }
        for (String file : ENVIRONMENTS_TO_POPULATE) {
            environmentPopulator.populate(file);
        }
        return SetupStats.builder()
                .numAdminUsers(adminUserService.count())
                .numEnvironments(environmentService.count())
                .build();
    }

    @Override
    public SetupStats populateFromString(String fileString, FilePopulateConfig config) throws IOException {
        return null;
    }

    /** This class is NOT persisted, despite extending BaseEntity */
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class SetupStats extends BaseEntity {
        private int numAdminUsers;
        private int numEnvironments;
    }
}
