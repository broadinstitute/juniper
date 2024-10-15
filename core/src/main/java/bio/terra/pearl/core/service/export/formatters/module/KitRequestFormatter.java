package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.KitTypeFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitRequestFormatter extends BeanListModuleFormatter<KitRequestDto> {
    private static final List<String> KIT_REQUEST_INCLUDED_PROPERTIES =
            List.of("status", "sentToAddress", "sentAt", "receivedAt", "createdAt", "labeledAt",
                    "trackingNumber", "returnTrackingNumber", "skipAddressValidation");

    public KitRequestFormatter(ExportOptions options) {
        super(options, "sample_kit", "Sample kit");
    }

    @Override
    protected List<PropertyItemFormatter<KitRequestDto>> generateItemFormatters(ExportOptions options) {
        itemFormatters = KIT_REQUEST_INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<>(propName, KitRequestDto.class))
                .collect(Collectors.toList());
        // we have to handle kitType separately because we'll need to match it to the kitType name
        itemFormatters.add(new KitTypeFormatter());
        return itemFormatters;
    }

    @Override
    public List<KitRequestDto> getBeans(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getKitRequests();
    }

    @Override
    public Comparator<KitRequestDto> getComparator() {
        return Comparator.comparing(KitRequestDto::getCreatedAt);
    }

    public List<KitRequestDto> listFromStringMap(Map<String, String> enrolleeMap) {
        List<KitRequestDto> kitRequests = new ArrayList<>();
        int requestNum = 1;
        KitRequestDto kitRequestDto = getKitRequestDto(enrolleeMap, requestNum);
        while (kitRequestDto != null) {
            kitRequests.add(kitRequestDto);
            requestNum++;
            kitRequestDto = getKitRequestDto(enrolleeMap, requestNum);
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
