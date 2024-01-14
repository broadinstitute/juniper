package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.KitRequestTypeFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import bio.terra.pearl.core.service.kit.KitRequestDto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitRequestFormatter extends ModuleFormatter<KitRequestDto, PropertyItemFormatter<KitRequestDto>> {
    private static final String KIT_REQUEST_MODULE_NAME = "sample_kit";
    private static final List<String> KIT_REQUEST_INCLUDED_PROPERTIES =
        List.of("status", "sentToAddress", "sentAt", "receivedAt");

    public KitRequestFormatter() {
        itemFormatters = KIT_REQUEST_INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<KitRequestDto>(propName, KitRequestDto.class))
                .collect(Collectors.toList());
        // we have to handle kitType separately because we'll need to match it to the kitType name
        itemFormatters.add(new KitRequestTypeFormatter());
        moduleName = KIT_REQUEST_MODULE_NAME;
        displayName = "Sample kit";
    }

    @Override
    public Map<String, String> toStringMap(EnrolleeExportData enrolleeData) {
        List<KitRequestDto> kitRequests = enrolleeData.getKitRequests();
        // sort the kits oldest first
        List<KitRequestDto> sortedKitRequests = kitRequests.stream()
                .sorted(Comparator.comparing(KitRequestDto::getCreatedAt)).toList();
        Map<String, String> allKitMap = new HashMap<>();
        for (int i = 0; i < sortedKitRequests.size(); i++) {
            for (PropertyItemFormatter<KitRequestDto> itemInfo : getItemFormatters()) {
                String value = itemInfo.getExportString(sortedKitRequests.get(i));
                String columnName = getColumnKey(itemInfo, false, null, i + 1);
                allKitMap.put(columnName, value);
            }
        }
        maxNumRepeats = Math.max(maxNumRepeats, sortedKitRequests.size());
        return allKitMap;
    }
}
