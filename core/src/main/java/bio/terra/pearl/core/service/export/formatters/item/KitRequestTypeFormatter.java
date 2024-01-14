package bio.terra.pearl.core.service.export.formatters.item;

import bio.terra.pearl.core.service.export.DataValueExportType;
import bio.terra.pearl.core.service.kit.KitRequestDto;

public class KitRequestTypeFormatter extends PropertyItemFormatter<KitRequestDto> {
    public KitRequestTypeFormatter() {
        super("kitType", KitRequestDto.class);
        this.baseColumnKey = "kitType";
        this.dataType = DataValueExportType.STRING;
    }

    @Override
    public String getExportString(KitRequestDto bean) {
        return bean.getKitType().getName();
    }
}
