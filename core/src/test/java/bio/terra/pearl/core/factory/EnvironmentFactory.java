package bio.terra.pearl.core.factory;

import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentFactory {
    @Autowired
    EnvironmentService environmentService;
    public Environment.EnvironmentBuilder builder(String testName) {
        return Environment.builder().name(EnvironmentName.sandbox);
    }

    public Environment buildPersisted(String testName) {
        return buildPersisted(builder(testName), testName);
    }

    public Environment buildPersisted(Environment.EnvironmentBuilder builder, String testName) {
        return environmentService.create(builder.build());
    }
}
