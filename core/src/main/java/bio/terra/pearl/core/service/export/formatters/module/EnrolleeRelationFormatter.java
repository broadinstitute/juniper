package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EnrolleeRelationFormatter extends BeanListModuleFormatter<EnrolleeRelation> {
    private static final List<String> INCLUDED_PROPERTIES =
            List.of("enrollee.shortcode", "relationshipType", "beginDate", "endDate", "familyRelationship", "family.shortcode");

    public EnrolleeRelationFormatter(ExportOptions exportOptions) {
        super(exportOptions, "relation",  "Relations");
    }

    @Override
    protected List<PropertyItemFormatter<EnrolleeRelation>> generateItemFormatters(ExportOptions options) {
        return INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<>(propName, EnrolleeRelation.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrolleeRelation> getBeans(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getEnrolleeRelations();
    }

    @Override
    public Comparator<EnrolleeRelation> getComparator() {
        return Comparator.comparing(EnrolleeRelation::getCreatedAt);
    }
}
