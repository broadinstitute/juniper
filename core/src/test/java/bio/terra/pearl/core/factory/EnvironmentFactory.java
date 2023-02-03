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
    public Environment.EnvironmentBuilder builder(String testName) {
        return Environment.builder().name(EnvironmentName.sandbox);
    }

    public Environment buildPersisted(String testName) {
        return buildPersisted(builder(testName), testName);
    }

    /** since there are only 3 possible environments, we can't always return a fresh one.  check to see
     * if the given environment exists, and either return it or a new one as needed.
     */
    public Environment buildPersisted(Environment.EnvironmentBuilder builder, String testName) {
        Optional<Environment> environment = environmentService.findOneByName(builder.build().getName());
        if (environment.isPresent()) {
            return environment.get();
        }
        return environmentService.create(builder.build());
    }
}
