package bio.terra.pearl.populate.service;

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
    private final AdminUserPopulator adminUserPopulator;
    private final AdminConfigPopulator adminConfigPopulator;
    private final AdminUserService adminUserService;
    private final LanguageTextPopulator languageTextPopulator;
    private final KitTypePopulator kitTypePopulator;
    private final RolePopulator rolePopulator;
    private final PermissionPopulator permissionPopulator;

    public static final List<String> ADMIN_USERS_TO_POPULATE =
            Arrays.asList("adminUsers/dbush.json", "adminUsers/myanaman.json",
                    "adminUsers/connor.json", "adminUsers/egwozdz.json",
                    "adminUsers/mflinn.json", "adminUsers/nwatts.json",
                    "adminUsers/mbemis.json", "adminUsers/cunningh.json",
                    "adminUsers/andrew.json", "adminUsers/pegah.json", "adminUsers/sampath.json");
    public static final List<String> LANGUAGE_TEXTS_TO_POPULATE =
            Arrays.asList("i18n/en/languageTexts.json", "i18n/es/languageTexts.json", "i18n/dev/languageTexts.json");

    public BaseSeedPopulator(AdminUserPopulator adminUserPopulator, AdminConfigPopulator adminConfigPopulator,
                             AdminUserService adminUserService, KitTypePopulator kitTypePopulator,
                             LanguageTextPopulator languageTextPopulator, PermissionPopulator permissionPopulator,
                             RolePopulator rolePopulator) {
        this.adminUserPopulator = adminUserPopulator;
        this.adminConfigPopulator = adminConfigPopulator;
        this.adminUserService = adminUserService;
        this.kitTypePopulator = kitTypePopulator;
        this.languageTextPopulator = languageTextPopulator;
        this.permissionPopulator = permissionPopulator;
        this.rolePopulator = rolePopulator;
    }

    public SetupStats populate(String filePathName) throws IOException {
        // for now, we ignore the pathname
        for (String file : ADMIN_USERS_TO_POPULATE) {
            adminUserPopulator.populate(new FilePopulateContext(file), false);
        }
        AdminConfigPopulator.AdminConfigStats configStats = adminConfigPopulator.populate(true);
        return SetupStats.builder()
                .numAdminUsers(adminUserService.count())
                .adminConfigStats(configStats)
                .build();
    }

    public void populateLanguageTexts() {
        for (String file : LANGUAGE_TEXTS_TO_POPULATE) {
            languageTextPopulator.populateList(new FilePopulateContext(file), false);
        }
    }

    public void populateRolesAndPermissions() {
        permissionPopulator.populateList(new FilePopulateContext("iam/permissions.json"), false);
        rolePopulator.populateList(new FilePopulateContext("iam/roles.json"), false);
    }

    public void populateKitTypes() {
        kitTypePopulator.populateList(new FilePopulateContext("kits/kitTypes.json"), false);
    }

    /** This class is NOT persisted, despite extending BaseEntity */
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class SetupStats {
        private int numAdminUsers;
        private AdminConfigPopulator.AdminConfigStats adminConfigStats;
    }
}
