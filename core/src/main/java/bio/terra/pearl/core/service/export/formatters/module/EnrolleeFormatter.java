package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class EnrolleeFormatter extends BeanModuleFormatter<Enrollee> {
    public static final String ENROLLEE_MODULE_NAME = "enrollee";
    public static final List<String> INCLUDED_PROPERTIES = List.of("shortcode", "consented", "createdAt");

    public EnrolleeFormatter(ExportOptions exportOptions) {
        itemFormatters = INCLUDED_PROPERTIES.stream().map(propName -> new PropertyItemFormatter<Enrollee>(propName, Enrollee.class))
                .collect(Collectors.toList());
        moduleName = ENROLLEE_MODULE_NAME;
        displayName = "Enrollee";
    }

    @Override
    public Enrollee getBean(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getEnrollee();
    }

    @Override
    public Enrollee newBean() {
        return new Enrollee();
    }
}
