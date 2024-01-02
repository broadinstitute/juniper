package bio.terra.pearl.core.model.participant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Facet for participant search. This consists of:
 *   keyName: unique name for facet within category
 *   facetType: the type of facet (currently the service only supports ENTITY_OPTIONS and STRING_OPTIONS)
 *   category: the category of the facet (for instance: profile, participantTask, etc.)
 *   label: the label to display for the facet
 *   entities: the list of entities for the facet, if more than one. A facet with multiple entities means a user
 *      can search on one or more entities of the same type (keyname, category), and the facet values are ANDed to
 *      produce a search result
 *   options: the list of options for the facet, if any. If there is an entity list the options apply to each entity.
 */
@Getter @Setter
@SuperBuilder
public class EnrolleeSearchFacet {
    public enum  FacetType {
        ENTITY_OPTIONS,
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
