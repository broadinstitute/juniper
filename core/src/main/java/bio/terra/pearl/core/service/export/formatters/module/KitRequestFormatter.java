package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.KitTypeFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitRequestFormatter extends ModuleFormatter<KitRequestDto, PropertyItemFormatter<KitRequestDto>> {
    private static final String KIT_REQUEST_MODULE_NAME = "sample_kit";
    private static final List<String> KIT_REQUEST_INCLUDED_PROPERTIES =
            List.of("status", "sentToAddress", "sentAt", "receivedAt", "createdAt", "labeledAt",
                    "trackingNumber", "returnTrackingNumber", "skipAddressValidation");

    public KitRequestFormatter() {
        itemFormatters = KIT_REQUEST_INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<KitRequestDto>(propName, KitRequestDto.class))
                .collect(Collectors.toList());
        // we have to handle kitType separately because we'll need to match it to the kitType name
        itemFormatters.add(new KitTypeFormatter());
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

    public List<KitRequestDto> listFromStringMap(Map<String, String> enrolleeMap) {
        List<KitRequestDto> kitRequests = new ArrayList<>();
        for (int requestNum = 1; requestNum < 10; requestNum++) {
            KitRequestDto kitRequestDto = getKitRequestDto(enrolleeMap, requestNum);
            if (kitRequestDto == null) {
                return kitRequests;
            }
            kitRequests.add(kitRequestDto);
        }
        return kitRequests;
    }

    private KitRequestDto getKitRequestDto(Map<String, String> enrolleeMap, int requestNum) {
        KitRequestDto kitRequestDto = null;
        for (PropertyItemFormatter<KitRequestDto> itemFormatter : itemFormatters) {
            String columnName = getColumnKey(itemFormatter, false, null, requestNum);
            String stringVal = enrolleeMap.get(columnName);
            if (StringUtils.isEmpty(stringVal)) {
                continue;
            }
            if (kitRequestDto == null) {
                kitRequestDto = new KitRequestDto();
            }
            if (columnName.contains(".status")) {
                //enum lookup
                kitRequestDto.setStatus(KitRequestStatus.valueOf(stringVal));
            } else {
                itemFormatter.importValueToBean(kitRequestDto, stringVal);
            }
        }
        return kitRequestDto;
    }

}
