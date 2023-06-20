package bio.terra.pearl.api.admin.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.controller.enrollee.EnrolleeSearchController;
import bio.terra.pearl.core.service.participant.search.facets.IntRangeFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.StableIdStringFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.StringFacetValue;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EnrolleeSearchControllerTest extends BaseSpringBootTest {
  @Autowired EnrolleeSearchController enrolleeSearchController;

  @Test
  public void testFacetsFromJsonEmpty() throws Exception {
    List<SqlSearchableFacet> facets = enrolleeSearchController.facetsFromJsonString(null);
    assertThat(facets, hasSize(0));

    facets = enrolleeSearchController.facetsFromJsonString("");
    assertThat(facets, hasSize(0));

    facets = enrolleeSearchController.facetsFromJsonString("{}");
    assertThat(facets, hasSize(0));
  }

  @Test
  public void testSimpleProfileFacet() throws Exception {
    List<SqlSearchableFacet> facets =
        enrolleeSearchController.facetsFromJsonString(
            "{\"profile\":{\"sexAtBirth\":{\"values\":[\"female\"]}}}");
    assertThat(facets, hasSize(1));
    StringFacetValue facetValue = (StringFacetValue) facets.get(0).getValue();
    assertThat(facetValue.getKeyName(), equalTo("sexAtBirth"));
    assertThat(facetValue.getValues(), hasItems("female"));
  }

  @Test
  public void testSimpleAgeFacet() throws Exception {
    List<SqlSearchableFacet> facets =
        enrolleeSearchController.facetsFromJsonString("{\"profile\":{\"age\":{\"min\":30}}}");
    assertThat(facets, hasSize(1));
    IntRangeFacetValue facetValue = (IntRangeFacetValue) facets.get(0).getValue();
    assertThat(facetValue.getKeyName(), equalTo("age"));
    assertThat(facetValue.getMin(), equalTo(30));
    assertThat(facetValue.getMax(), equalTo(null));
  }

  @Test
  public void testTaskStatusFacet() throws Exception {
    List<SqlSearchableFacet> facets =
        enrolleeSearchController.facetsFromJsonString(
            "{\"participantTask\":{\"status\":{\"values\":[{\"stableId\":\"oh_oh_consent\",\"values\":[\"COMPLETE\"]}]}}}");
    assertThat(facets, hasSize(1));
    StableIdStringFacetValue facetValue = (StableIdStringFacetValue) facets.get(0).getValue();
    assertThat(facetValue.getKeyName(), equalTo("status"));
    assertThat(facetValue.getStableId(), equalTo("oh_oh_consent"));
    assertThat(facetValue.getValues(), hasItems("COMPLETE"));
  }
}
