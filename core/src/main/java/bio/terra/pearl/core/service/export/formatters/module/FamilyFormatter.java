package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FamilyFormatter extends BeanListModuleFormatter<Family> {
    private static final String MODULE_NAME = "family";
    private static final List<String> INCLUDED_PROPERTIES =
        List.of("shortcode", "proband.shortcode");

    public FamilyFormatter() {
        itemFormatters = INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<>(propName, Family.class))
                .collect(Collectors.toList());
        moduleName = MODULE_NAME;
        displayName = "Families";
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
