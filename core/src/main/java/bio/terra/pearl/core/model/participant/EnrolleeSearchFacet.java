package bio.terra.pearl.core.model.participant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder
public class EnrolleeSearchFacet {
    public enum  FacetType {
        STABLEID_STRING,
        STRING_OPTIONS
    }

    private final String keyName;
    private final FacetType facetType;
    private final String category;
    private final String label;
    private final List<ValueLabel> entities;
    private final List<ValueLabel> options;

    public EnrolleeSearchFacet(String keyName, FacetType facetType, String category, String label) {
        this.keyName = keyName;
        this.facetType = facetType;
        this.category = category;
        this.label = label;
        this.entities = new ArrayList<>();
        this.options = new ArrayList<>();
    }

    public void addEntity(String value, String label) {
        entities.add(new ValueLabel(value, label));
    }

    public void addEntities(Map<String, String> entities) {
        for (Map.Entry<String, String> entry : entities.entrySet()) {
            this.entities.add(new ValueLabel(entry.getKey(), entry.getValue()));
        }
    }

    public void addOptions(Map<String, String> entityValues) {
        for (Map.Entry<String, String> entry : entityValues.entrySet()) {
            this.options.add(new ValueLabel(entry.getKey(), entry.getValue()));
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ValueLabel {
        private final String value;
        private final String label;
    }
}
