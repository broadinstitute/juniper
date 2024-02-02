package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.service.EnvironmentService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentFactory {
    @Autowired
    EnvironmentService environmentService;

    public Environment buildPersisted(String testName) {
        return buildPersisted(testName, EnvironmentName.sandbox);
    }

    public Environment buildPersisted(String testName, EnvironmentName envName) {
        Optional<Environment> environment = environmentService.findOneByName(envName);
        if (environment.isPresent()) {
            return environment.get();
        }
        return environmentService.create(Environment.builder().name(envName).build());
    }



}
