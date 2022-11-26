package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.service.EnvironmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class EnvironmentPopulator {
    private ObjectMapper objectMapper;
    private EnvironmentService environmentService;
    private FilePopulateService filePopulateService;

    public EnvironmentPopulator(ObjectMapper objectMapper, EnvironmentService environmentService, FilePopulateService filePopulateService) {
        this.objectMapper = objectMapper;
        this.environmentService = environmentService;
        this.filePopulateService = filePopulateService;
    }

    @Transactional
    public Environment populate(String filePathName) throws IOException {
        FilePopulateConfig config = new FilePopulateConfig(filePathName);
        String portalFileString = filePopulateService.readFile(config.getRootFileName(), config);
        return populateFromString(portalFileString, config);
    }

    protected Environment populateFromString(String envContent, FilePopulateConfig config)  throws IOException {
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
