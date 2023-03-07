package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.service.EnvironmentService;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvironmentPopulator extends Populator<Environment, FilePopulateContext> {
    private EnvironmentService environmentService;

    public EnvironmentPopulator(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @Transactional
    public Environment populate(String filePathName) throws IOException {
        FilePopulateContext config = new FilePopulateContext(filePathName);
        String portalFileString = filePopulateService.readFile(config.getRootFileName(), config);
        return populateFromString(portalFileString, config);
    }

    public Environment populateFromString(String envContent, FilePopulateContext context)  throws IOException {
        Environment environment = objectMapper.readValue(envContent, Environment.class);
        Optional<Environment> existingEnvironment = environmentService.findOneByName(environment.getName());
        existingEnvironment.ifPresentOrElse(env -> {
            String noop = "noop";
        }, () -> {
            environmentService.create(environment);
        });
        return environment;
    }
}
