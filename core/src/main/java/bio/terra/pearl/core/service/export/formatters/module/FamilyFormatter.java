package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FamilyFormatter extends BeanListModuleFormatter<Family> {
    private static final List<String> INCLUDED_PROPERTIES =
        List.of("shortcode", "proband.shortcode");

    public FamilyFormatter(ExportOptions options) {
        super(options, "family", "Families");
    }

    @Override
    protected List<PropertyItemFormatter<Family>> generateItemFormatters(ExportOptions options) {
        return INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<>(propName, Family.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<Family> getBeans(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getFamilies();
    }

    @Override
    public Comparator<Family> getComparator() {
        return Comparator.comparing(Family::getCreatedAt);
    }
}
