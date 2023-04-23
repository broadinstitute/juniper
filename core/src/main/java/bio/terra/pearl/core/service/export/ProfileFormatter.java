package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.participant.MailingAddress;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ProfileFormatter implements ExportFormatter {
    private static final String PROFILE_MODULE_NAME = "profile";
    private static final String ADDRESS_SUBMODULE_NAME = "address";
    private static final List<String> PROFILE_EXCLUDED_PROPERTIES = List.of("id", "createdAt",
            "lastUpdatedAt", "mailingAddress", "mailingAddressId", "class");
    private static final List<String> MAILING_ADDRESS_EXCLUDED_PROPERTIES = List.of("id", "createdAt",
            "lastUpdatedAt", "class");

    @Override
    public Map<String, String> toStringMap(EnrolleeExportData exportData, ModuleExportInfo moduleInfo) throws Exception {
        Profile profile = exportData.getProfile();
        return ExportFormatUtils.mapBeanForExport(profile, moduleInfo);
    }

    @Override
    public String getColumnKey(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        return itemExportInfo.getBaseColumnKey();
    }

    @Override
    public String getColumnHeader(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        return itemExportInfo.getBaseColumnKey();
    }

    @Override
    public String getColumnSubHeader(ModuleExportInfo moduleExportInfo, ItemExportInfo itemExportInfo, boolean isOtherDescription, QuestionChoice choice) {
        return ExportFormatUtils.camelToWordCase(itemExportInfo.getPropertyAccessor());
    }

    public ModuleExportInfo getModuleExportInfo(ExportOptions exportOptions) throws Exception {
        List<ItemExportInfo> itemInfo = ExportFormatUtils.getIncludedProperties(Profile.class, PROFILE_EXCLUDED_PROPERTIES)
                .stream().map(propName -> ExportFormatUtils.getItemInfoForBeanProp(PROFILE_MODULE_NAME, propName))
                .collect(Collectors.toList());
        itemInfo.addAll(ExportFormatUtils.getIncludedProperties(MailingAddress.class, MAILING_ADDRESS_EXCLUDED_PROPERTIES)
                .stream().map(propName -> ExportFormatUtils.getItemInfoForBeanProp(PROFILE_MODULE_NAME, "mailingAddress." + propName))
                .toList());
        return ModuleExportInfo.builder()
                .moduleName(PROFILE_MODULE_NAME)
                .items(itemInfo)
                .formatter(this)
                .build();
    }

}
