package bio.terra.pearl.api.admin.config;

import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InitialPopulate {
  Logger log = LoggerFactory.getLogger(InitialPopulate.class);
  @Autowired private AdminUserService adminUserService;
  @Autowired private BaseSeedPopulator baseSeedPopulator;

  @EventListener(ApplicationReadyEvent.class)
  public void populateSeed() throws IOException {
    log.info("Refreshing base seed populate data");
    baseSeedPopulator.populate();
  }
}
