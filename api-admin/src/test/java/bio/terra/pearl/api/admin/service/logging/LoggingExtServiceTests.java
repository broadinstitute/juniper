package bio.terra.pearl.api.admin.service.logging;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class LoggingExtServiceTests extends BaseSpringBootTest {

  @Autowired private LoggingExtService loggingExtService;

//  @Test
//  public void testAllSuperUserOnly() {
//    AuthTestUtils.verifySuperUserOnlyMethods(
//        loggingExtService, AuthAnnotationSpec.withPortalPerm("logging"));
//  }
}
