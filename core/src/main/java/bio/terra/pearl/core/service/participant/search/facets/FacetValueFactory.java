package bio.terra.pearl.core.service.participant.search.facets;

import bio.terra.pearl.core.service.participant.search.facets.sql.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FacetValueFactory {
  private ObjectMapper objectMapper;
  private static final Map<String, Map<String, FacetDefinition>> facetTypeMap = Map.of(
      "profile", Map.of(
          "age", new FacetDefinition(IntRangeFacetValue.class, new ProfileAgeFacetSqlGenerator()),
          "sexAtBirth", new FacetDefinition(StringFacetValue.class, new ProfileFacetSqlGenerator())
      ),
      "participantTask", Map.of(
          "status", new FacetDefinition(CombinedStableIdFacetValue.class, new ParticipantTaskFacetSqlGenerator())
      ),
      "keyword", Map.of(
              "keyword", new FacetDefinition(StringFacetValue.class, new KeywordFacetSqlGenerator())
      ),
      "survey", Map.of(
              "answer", new FacetDefinition(AnswerFacetValue.class, new AnswerFacetSqlGenerator())
      )
  );

  public FacetValueFactory(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public SqlSearchableFacet fromJson(String categoryName, String keyName, JsonNode value) {
    FacetDefinition<FacetValue> facetDef = facetTypeMap.get(categoryName).get(keyName);
    if (facetDef == null) {
      throw new IllegalArgumentException("no matching facet for category/key %s/%s".formatted(categoryName, keyName));
    }
    FacetValue facetValue = objectMapper.convertValue(value, facetDef.getValueClass());
    facetValue.setKeyName(keyName);
    return new SqlSearchableFacet(facetValue, facetDef.getSqlGenerator());
  }
}
