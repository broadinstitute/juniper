package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.service.EnvironmentService;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
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
public class BaseSeedPopulator {
    private AdminUserPopulator adminUserPopulator;
    private EnvironmentPopulator environmentPopulator;
    private AdminConfigPopulator adminConfigPopulator;
    private AdminUserService adminUserService;
    private EnvironmentService environmentService;
    private KitTypePopulator kitTypePopulator;

    public static final List<String> ADMIN_USERS_TO_POPULATE =
            Arrays.asList("adminUsers/dbush.json", "adminUsers/myanaman.json",
                    "adminUsers/jkorte.json", "adminUsers/egwozdz.json",
                    "adminUsers/mflinn.json", "adminUsers/nwatts.json",
                    "adminUsers/mbemis.json", "adminUsers/cunningh.json",
                    "adminUsers/andrew.json");
    public static final List<String> ENVIRONMENTS_TO_POPULATE =
            Arrays.asList("environments/sandbox.json", "environments/irb.json", "environments/live.json");

    public BaseSeedPopulator(AdminUserPopulator adminUserPopulator, EnvironmentPopulator environmentPopulator,
                             AdminConfigPopulator adminConfigPopulator, AdminUserService adminUserService,
                             EnvironmentService environmentService, KitTypePopulator kitTypePopulator) {
        this.adminUserPopulator = adminUserPopulator;
        this.environmentPopulator = environmentPopulator;
        this.adminConfigPopulator = adminConfigPopulator;
        this.adminUserService = adminUserService;
        this.environmentService = environmentService;
        this.kitTypePopulator = kitTypePopulator;
    }

    public SetupStats populate(String filePathName) throws IOException {
        // for now, we ignore the pathname
        for (String file : ADMIN_USERS_TO_POPULATE) {
            adminUserPopulator.populate(new FilePopulateContext(file), false);
        }
        for (String file : ENVIRONMENTS_TO_POPULATE) {
            environmentPopulator.populate(new FilePopulateContext(file), false);
        }
        var configStats = adminConfigPopulator.populate(true);
        return SetupStats.builder()
                .numAdminUsers(adminUserService.count())
                .numEnvironments(environmentService.count())
                .adminConfigStats(configStats)
                .build();
    }

    /** This class is NOT persisted, despite extending BaseEntity */
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class SetupStats {
        private int numAdminUsers;
        private int numEnvironments;
        private AdminConfigPopulator.AdminConfigStats adminConfigStats;
    }
}
