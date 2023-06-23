package bio.terra.pearl.core.service.participant.search.facets;

/** A set of values that are parameters for a facet. */
public interface FacetValue {
  String getKeyName();
  void setKeyName(String keyName);
}
