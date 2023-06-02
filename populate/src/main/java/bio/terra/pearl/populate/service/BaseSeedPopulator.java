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
 * Populates entities configs needed for a server instance, including environments and admin users.
 * If those users/items exist, they will be updated, rather than duplicated.
 *
 * this is intended to capture essential populate data -- it will be checked every time a server starts
 * so avoid putting things in this file unless they are essential to server operations, and not customizable by users
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
            Arrays.asList("adminUsers/dbush.json", "adminUsers/breilly.json",
                    "adminUsers/myanaman.json", "adminUsers/kkaratza.json",
                    "adminUsers/jkorte.json", "adminUsers/egwozdz.json",
                    "adminUsers/mflinn.json", "adminUsers/nwatts.json",
                    "adminUsers/mbemis.json");
    public static final List<String> ENVIRONMENTS_TO_POPULATE =
            Arrays.asList("environments/sandbox.json", "environments/irb.json", "environments/live.json");
    public static final List<String> KIT_TYPES_TO_POPULATE =
            List.of("kits/salivaKitType.json");

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

    /**
     * BE CAREFUL!!!
     * This method runs every time a server instance starts.  So avoid things that might take a long time,
     * or that might overwrite user-entered data
     */
    public SetupStats populate() throws IOException {
        // for now, we ignore the pathname
        for (String file : ADMIN_USERS_TO_POPULATE) {
            adminUserPopulator.populate(new FilePopulateContext(file), false);
        }
        for (String file : ENVIRONMENTS_TO_POPULATE) {
            environmentPopulator.populate(new FilePopulateContext(file), false);
        }
        for (String file : KIT_TYPES_TO_POPULATE) {
            kitTypePopulator.populate(new FilePopulateContext(file), false);
        }
        // overwrite is ok here -- we're not versioning admin emails (currently just the "here's the link to
        // the admin tool" welcome email to ne study staff
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
