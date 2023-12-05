package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.item.KitRequestTypeFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitRequestFormatter extends ModuleFormatter<KitRequest, PropertyItemFormatter<KitRequest>> {
    private static final String KIT_REQUEST_MODULE_NAME = "sample_kit";
    private static final List<String> KIT_REQUEST_INCLUDED_PROPERTIES = List.of("status", "sentToAddress", "externalKit");

    private static final String KIT_TYPE_COLUMN_NAME = "kitType";

    public KitRequestFormatter(List<KitType> kitTypes) {
        itemFormatters = KIT_REQUEST_INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<KitRequest>(propName, KitRequest.class))
                .collect(Collectors.toList());
        // we have to handle kitType separately because we'll need to match it to the kitType name
        itemFormatters.add(new KitRequestTypeFormatter(kitTypes));
        moduleName = KIT_REQUEST_MODULE_NAME;
        displayName = "Sample kit";
    }

    @Override
    public Map<String, String> toStringMap(EnrolleeExportData enrolleeData) {
        List<KitRequest> kitRequests = enrolleeData.getKitRequests();
        // sort the kits oldest first
        List<KitRequest> sortedKitRequests = kitRequests.stream()
                .sorted(Comparator.comparing(KitRequest::getCreatedAt)).toList();
        Map<String, String> allKitMap = new HashMap<>();
        for (int i = 0; i < sortedKitRequests.size(); i++) {
            for (PropertyItemFormatter<KitRequest> itemInfo : getItemFormatters()) {
                String value = itemInfo.getExportString(sortedKitRequests.get(i));
                String columnName = getColumnKey(itemInfo, false, null, i + 1);
                allKitMap.put(columnName, value);
            }
        }
        maxNumRepeats = Math.max(maxNumRepeats, sortedKitRequests.size());
        return allKitMap;
    }
}
