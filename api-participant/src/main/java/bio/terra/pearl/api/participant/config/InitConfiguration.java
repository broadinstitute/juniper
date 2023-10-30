package bio.terra.pearl.api.participant.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitConfiguration {
  @Autowired private B2CConfigurationService b2CConfigurationService;

  @Value("${b2c-config-file}")
  String b2cConfigFile;

  @EventListener(ApplicationReadyEvent.class)
  public void initialize() {
    b2CConfigurationService.initB2CConfig(b2cConfigFile);
  }
}
