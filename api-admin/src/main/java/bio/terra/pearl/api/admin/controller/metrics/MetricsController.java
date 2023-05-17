package bio.terra.pearl.api.admin.controller.metrics;

import bio.terra.pearl.api.admin.api.MetricsApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.MetricsExtService;
import bio.terra.pearl.core.dao.metrics.MetricName;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class MetricsController implements MetricsApi {
  private AuthUtilService authUtilService;
  private MetricsExtService metricsExtService;
  private HttpServletRequest request;

  public MetricsController(
      AuthUtilService authUtilService,
      MetricsExtService metricsExtService,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.metricsExtService = metricsExtService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> metricByName(
      String portalShortcode, String studyShortcode, String envName, String metricName) {
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    MetricName metric = MetricName.valueOf(metricName.toUpperCase());
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    var result =
        metricsExtService.loadMetrics(
            adminUser, portalShortcode, studyShortcode, environmentName, metric);
    return ResponseEntity.ok(result);
  }
}
