package bio.terra.pearl.core.service.export.formatters.item;

import bio.terra.pearl.core.service.export.DataValueExportType;
import bio.terra.pearl.core.service.kit.KitRequestDetails;

public class KitRequestTypeFormatter extends PropertyItemFormatter<KitRequestDetails> {
    public KitRequestTypeFormatter() {
        super("kitType", KitRequestDetails.class);
        this.baseColumnKey = "kitType";
        this.dataType = DataValueExportType.STRING;
    }

    @Override
    public String getExportString(KitRequestDetails bean) {
        return bean.getKitType().getName();
    }
}
