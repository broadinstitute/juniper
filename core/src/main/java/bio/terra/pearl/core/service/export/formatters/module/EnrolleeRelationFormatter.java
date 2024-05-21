package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnrolleeRelationFormatter extends ModuleFormatter<EnrolleeRelation, PropertyItemFormatter<EnrolleeRelation>> {
    private static final String MODULE_NAME = "proxy";
    private static final List<String> INCLUDED_PROPERTIES =
        List.of("enrollee.shortcode", "relationshipType", "beginDate", "endDate");

    public EnrolleeRelationFormatter() {
        itemFormatters = INCLUDED_PROPERTIES.stream()
                .map(propName -> new PropertyItemFormatter<>(propName, EnrolleeRelation.class))
                .collect(Collectors.toList());
        // we have to handle kitType separately because we'll need to match it to the kitType name
        moduleName = MODULE_NAME;
        displayName = "Proxy Relations";
    }

    // near-duplicate of toStringMap in KitRequestFormatter, potentially could
    // extract helper methods if more similar formatters arise
    @Override
    public Map<String, String> toStringMap(EnrolleeExportData enrolleeData) {
        List<EnrolleeRelation> relations = enrolleeData.getEnrolleeRelations();
        relations = relations.stream()
                .sorted(Comparator.comparing(EnrolleeRelation::getCreatedAt)).toList();
        Map<String, String> outputMap = new HashMap<>();
        for (int i = 0; i < relations.size(); i++) {
            for (PropertyItemFormatter<EnrolleeRelation> itemInfo : getItemFormatters()) {
                String value = itemInfo.getExportString(relations.get(i));
                String columnName = getColumnKey(itemInfo, false, null, i + 1);
                outputMap.put(columnName, value);
            }
        }
        maxNumRepeats = Math.max(maxNumRepeats, relations.size());
        return outputMap;
    }
}
