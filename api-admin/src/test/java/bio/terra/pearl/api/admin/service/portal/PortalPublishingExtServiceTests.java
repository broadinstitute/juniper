package bio.terra.pearl.api.admin.service.portal;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PortalPublishingExtServiceTests extends BaseSpringBootTest {
  @Autowired private PortalPublishingExtService portalPublishingExtService;

  @Test
  public void testPermissions() {
    AuthTestUtils.assertAllMethodsAnnotated(
        portalPublishingExtService,
        Map.of(
            "diff", AuthAnnotationSpec.withPortalPerm("BASE"),
            "publish", AuthAnnotationSpec.withPortalPerm("publish"),
            "getChangeRecords", AuthAnnotationSpec.withPortalPerm("BASE")));
  }
}
