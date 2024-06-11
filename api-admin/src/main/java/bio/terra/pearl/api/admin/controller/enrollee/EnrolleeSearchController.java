package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.EnrolleeSearchApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeExtService;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeSearchExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.EnrolleeSearchFacet;
import bio.terra.pearl.core.model.participant.EnrolleeSearchResult;
import bio.terra.pearl.core.service.participant.search.facets.FacetValueFactory;
import bio.terra.pearl.core.service.participant.search.facets.sql.SqlSearchableFacet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class EnrolleeSearchController implements EnrolleeSearchApi {
  private AuthUtilService authUtilService;
  private EnrolleeExtService enrolleeExtService;
  private EnrolleeSearchExtService enrolleeSearchExtService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private FacetValueFactory facetValueFactory;

  public EnrolleeSearchController(
      AuthUtilService authUtilService,
      EnrolleeExtService enrolleeExtService,
      EnrolleeSearchExtService enrolleeSearchExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      FacetValueFactory facetValueFactory) {
    this.authUtilService = authUtilService;
    this.enrolleeExtService = enrolleeExtService;
    this.enrolleeSearchExtService = enrolleeSearchExtService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.facetValueFactory = facetValueFactory;
  }

  @Override
  public ResponseEntity<Object> search(
      String portalShortcode, String studyShortcode, String envName, String facetString) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser adminUser = authUtilService.requireAdminUser(request);

    List<SqlSearchableFacet> facetValues;
    try {
      facetValues = facetsFromJsonString(facetString);
    } catch (Exception e) {
      return ResponseEntity.unprocessableEntity().body(e.getMessage());
    }
    List<EnrolleeSearchResult> results =
        enrolleeExtService.search(
            adminUser, portalShortcode, studyShortcode, environmentName, facetValues);
    return ResponseEntity.ok(results);
  }

  @Override
  public ResponseEntity<Object> getSearchFacets(
      String portalShortcode, String studyShortcode, String envName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    AdminUser adminUser = authUtilService.requireAdminUser(request);

    List<EnrolleeSearchFacet> facets =
        enrolleeExtService.getSearchFacets(
            adminUser, portalShortcode, studyShortcode, environmentName);
    return ResponseEntity.ok(facets);
  }

  public List<SqlSearchableFacet> facetsFromJsonString(String facetString)
      throws JsonProcessingException {
    if (StringUtils.isEmpty(facetString)) {
      facetString = "{}";
    }
    JsonNode facetsNode = objectMapper.readTree(facetString);
    List<SqlSearchableFacet> facetValues = new ArrayList<>();
    facetsNode
        .fields()
        .forEachRemaining(
            entry -> {
              String categoryName = entry.getKey();
              List<SqlSearchableFacet> categoryValues =
                  parseCategory(categoryName, entry.getValue());
              facetValues.addAll(categoryValues);
            });
    return facetValues;
  }

  List<SqlSearchableFacet> parseCategory(String categoryName, JsonNode category) {
    List<SqlSearchableFacet> facetValues = new ArrayList<>();
    category
        .fields()
        .forEachRemaining(
            entry -> {
              String keyName = entry.getKey();
              SqlSearchableFacet facetValue =
                  facetValueFactory.fromJson(categoryName, keyName, entry.getValue());
              facetValues.add(facetValue);
            });
    return facetValues;
  }

  @Override
  public ResponseEntity<Object> getExpressionSearchFacets(
      String portalShortcode, String studyShortcode, String envName) {
    AdminUser user = authUtilService.requireAdminUser(request);
    return ResponseEntity.ok(
        this.enrolleeSearchExtService.getExpressionSearchFacets(
            user, portalShortcode, studyShortcode, EnvironmentName.valueOf(envName)));
  }
}
