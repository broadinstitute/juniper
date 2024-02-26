package bio.terra.pearl.core.service.participant.search.facets;

import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;

public class FacetExpression {
    private SqlSearchableFacet leftFacet;
    private FacetExpression leftExp;
    private String join;
    private SqlSearchableFacet rightFacet;
    private FacetExpression rightExp;
}
