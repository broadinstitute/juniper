package bio.terra.javatemplate.iam;

import bio.terra.common.iam.BearerToken;
import bio.terra.common.sam.SamRetry;
import bio.terra.common.sam.exception.SamExceptionFactory;
import bio.terra.javatemplate.model.SystemStatusSystems;
import java.util.List;
import org.broadinstitute.dsde.workbench.client.sam.ApiException;
import org.broadinstitute.dsde.workbench.client.sam.model.SystemStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SamService {
  private static final Logger logger = LoggerFactory.getLogger(SamService.class);
  private final SamClient samClient;

  @Autowired
  public SamService(SamClient samClient) {
    this.samClient = samClient;
  }

  public boolean getAction(
      String resourceType, String resourceId, String action, BearerToken bearerToken) {
    try {
      return SamRetry.retry(
          () ->
              samClient
                  .resourcesApi(bearerToken.getToken())
                  .resourcePermissionV2(resourceType, resourceId, action));
    } catch (ApiException e) {
      throw SamExceptionFactory.create(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw SamExceptionFactory.create("Sam retry interrupted", e);
    }
  }

  public SystemStatusSystems status() {
    // No access token needed since this is an unauthenticated API.
    try {
      // Don't retry status check
      SystemStatus samStatus = samClient.statusApi().getSystemStatus();
      var result = new SystemStatusSystems().ok(samStatus.getOk());
      var samSystems = samStatus.getSystems();
      // Populate error message if Sam status is non-ok
      if (result.isOk() == null || !result.isOk()) {
        String errorMsg = "Sam status check failed. Messages = " + samSystems;
        logger.error(errorMsg);
        result.addMessagesItem(errorMsg);
      }
      return result;
    } catch (Exception e) {
      String errorMsg = "Sam status check failed";
      logger.error(errorMsg, e);
      return new SystemStatusSystems().ok(false).messages(List.of(errorMsg));
    }
  }
}
