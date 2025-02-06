package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

public class EnrolleeRelationFormatter extends BeanListModuleFormatter<EnrolleeRelationFormatter.EnrolleeRelationExport> {
    private static final List<String> INCLUDED_PROPERTIES =
            List.of("otherEnrolleeShortcode", "relationship", "beginDate", "endDate", "familyRelationship", "family.shortcode");

    public EnrolleeRelationFormatter(ExportOptions exportOptions) {
        super(exportOptions, "relation",  "Relations");
    }

    @Override
    protected List<PropertyItemFormatter<EnrolleeRelationExport>> generateItemFormatters(ExportOptions options) {
        return INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<>(propName, EnrolleeRelationExport.class, options.getZoneId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrolleeRelationExport> getBeans(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getEnrolleeRelations().stream().map(relation -> {
                    EnrolleeRelationExport relationExport = new EnrolleeRelationExport();
                    BeanUtils.copyProperties(relation, relationExport);
                    relationExport.setExportingEnrolleeId(enrolleeExportData.getEnrollee().getId());
                    return relationExport;
                }).toList();
    }

    @Override
    public Comparator<EnrolleeRelationExport> getComparator() {
        return Comparator.comparing(EnrolleeRelation::getCreatedAt);
    }


    /**
     * helper class for exporting relations so that columns can be formatted depending on whether the
     * enrollee is the source or target of the relation
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class EnrolleeRelationExport extends EnrolleeRelation {
        private UUID exportingEnrolleeId; // the enrollee corresponding to the current export row
        /** the shortcode of the enrollee in the relation who is not the exporting enrollee row */
        public String getOtherEnrolleeShortcode() {
            return exportingEnrolleeId.equals(getEnrolleeId()) ? getTargetEnrollee().getShortcode() : getEnrollee().getShortcode();
        }

        /** gets the type, but with description indicating directionality where appropriate */
        public String getRelationship() {
            if (RelationshipType.isProxy(getRelationshipType())) {
                return exportingEnrolleeId.equals(getTargetEnrolleeId() ) ? "PROXY" : "PROXY_FOR";
            }
            return getRelationshipType().toString();
        }
    }
}
