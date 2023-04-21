package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.participant.Profile;
import java.util.List;
import java.util.Map;

public class ProfileFormatter {
    private static final String PROFILE_MODULE_NAME = "profile";
    private static final String ADDRESS_SUBMODULE_NAME = "address";
    private static final List<String> PROFILE_EXCLUDED_PROPERTIES = List.of("id", "createdAt",
            "lastUpdatedAt", "mailingAddress", "mailingAddressId", "class");
    private static final List<String> MAILING_ADDRESS_EXCLUDED_PROPERTIES = List.of("id", "createdAt",
            "lastUpdatedAt");

    public static Map<String, String> mapProfile(Profile profile) throws Exception {
        var valueMap = ExportFormatUtils.mapBeanForExport(profile, PROFILE_EXCLUDED_PROPERTIES, PROFILE_MODULE_NAME);
        valueMap.putAll(ExportFormatUtils.mapBeanForExport(profile.getMailingAddress(),
                MAILING_ADDRESS_EXCLUDED_PROPERTIES,
                PROFILE_MODULE_NAME + ExportFormatUtils.COLUMN_NAME_DELIMITER + ADDRESS_SUBMODULE_NAME));
        return valueMap;
    }

}
