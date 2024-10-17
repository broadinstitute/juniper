package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class EnrolleeFormatter extends BeanModuleFormatter<Enrollee> {
    public static final List<String> INCLUDED_PROPERTIES = List.of(
            "shortcode", "consented", "createdAt", "subject", "source"
    );

    public EnrolleeFormatter(ExportOptions exportOptions) {
        super(exportOptions, "enrollee", "Enrollee");
    }

    @Override
    protected List<PropertyItemFormatter<Enrollee>> generateItemFormatters(ExportOptions options) {
        return INCLUDED_PROPERTIES.stream().map(propName -> new PropertyItemFormatter<>(propName, Enrollee.class))
                .collect(Collectors.toList());
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
