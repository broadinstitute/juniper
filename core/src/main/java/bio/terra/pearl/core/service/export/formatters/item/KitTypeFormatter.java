package bio.terra.pearl.core.service.export.formatters.item;

import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.service.export.DataValueExportType;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

@Slf4j
public class KitTypeFormatter extends PropertyItemFormatter<KitRequestDto> {
    public KitTypeFormatter() {
        super("kitType", KitRequestDto.class);
        this.baseColumnKey = "kitType";
        this.dataType = DataValueExportType.STRING;
    }

    @Override
    public String getExportString(KitRequestDto bean) {
        return bean.getKitType().getName();
    }

    @Override
    public void importValueToBean(KitRequestDto bean, String exportString) {
        if (exportString != null) {
            try {
                PropertyUtils.setNestedProperty(bean, getPropertyName(), KitType.builder().name(exportString).build());
            } catch (Exception e) {
                log.warn("error setting property " + getPropertyName(), e);
            }
        }
    }

}
