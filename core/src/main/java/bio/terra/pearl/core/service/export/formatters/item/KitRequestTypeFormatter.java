package bio.terra.pearl.core.service.export.formatters.item;

import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.service.export.DataValueExportType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class KitRequestTypeFormatter extends PropertyItemFormatter<KitRequest> {
    private final Map<UUID, String> kitTypeNames;
    public KitRequestTypeFormatter(List<KitType> kitTypes) {
        super("kitType", KitRequest.class);
        this.kitTypeNames = kitTypes.stream().collect(Collectors.toMap(KitType::getId, KitType::getName));
        this.baseColumnKey = "kitType";
        this.dataType = DataValueExportType.STRING;
    }

    @Override
    public String getExportString(KitRequest bean) {
        return kitTypeNames.get(bean.getKitTypeId());
    }
}
