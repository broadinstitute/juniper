package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.address.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.List;
import java.util.stream.Collectors;


public class ProfileFormatter extends BeanModuleFormatter<Profile> {
    private static final String PROFILE_MODULE_NAME = "profile";
    private static final String ADDRESS_SUBMODULE_NAME = "address";
    private static final List<String> PROFILE_EXCLUDED_PROPERTIES = List.of("id", "createdAt",
            "lastUpdatedAt", "mailingAddress", "mailingAddressId", "class");
    private static final List<String> MAILING_ADDRESS_EXCLUDED_PROPERTIES = List.of("id", "createdAt",
            "lastUpdatedAt", "class");

    @Override
    public Profile getBean(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getProfile();
    }

    public ProfileFormatter(ExportOptions exportOptions) {
        itemFormatters = ExportFormatUtils.getIncludedProperties(Profile.class, PROFILE_EXCLUDED_PROPERTIES)
                .stream().map(propName -> new PropertyItemFormatter<Profile>(propName, Profile.class))
                .collect(Collectors.toList());
        itemFormatters.addAll(ExportFormatUtils.getIncludedProperties(MailingAddress.class, MAILING_ADDRESS_EXCLUDED_PROPERTIES)
                .stream().map(propName -> new PropertyItemFormatter<Profile>("mailingAddress." + propName, Profile.class))
                .toList());
        moduleName = PROFILE_MODULE_NAME;
        displayName = "Enrollee profile";
    }
}
