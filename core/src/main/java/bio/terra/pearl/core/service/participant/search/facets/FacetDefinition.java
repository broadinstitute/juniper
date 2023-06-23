package bio.terra.pearl.core.service.participant.search.facets;

import bio.terra.pearl.core.service.participant.search.facets.sql.FacetSqlGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class FacetDefinition<T extends FacetValue> {
  private Class<T> valueClass;
  private FacetSqlGenerator<T> sqlGenerator;
}
