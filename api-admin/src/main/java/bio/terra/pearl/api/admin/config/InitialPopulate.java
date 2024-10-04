package bio.terra.pearl.api.admin.config;

import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitialPopulate {
  @Autowired private AdminUserService adminUserService;
  @Autowired private BaseSeedPopulator baseSeedPopulator;

  @EventListener(ApplicationReadyEvent.class)
  public void populateSeedIfNeeded() throws IOException {
    if (isJUnitTest()) {
      log.info("Skipping seed populate for JUnit test");
      return;
    }
    if (adminUserService.count() == 0) {
      log.info("No admin users found, populating base");
      baseSeedPopulator.populate("seed");
    } else {
      log.info("Existing admin users found, skipping seed populate");
    }

    log.info("Repopulating core language texts");
    baseSeedPopulator.populateLanguageTexts();
    log.info("Repopulating roles and permissions");
    baseSeedPopulator.populateRolesAndPermissions();
    log.info("Repopulating kit types");
    baseSeedPopulator.populateKitTypes();
  }

  public static boolean isJUnitTest() {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      if (element.getClassName().startsWith("org.junit.")) {
        return true;
      }
    }
    return false;
  }
}
