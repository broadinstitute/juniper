package bio.terra.pearl.populate.service;

import bio.terra.pearl.populate.dto.FilePopulatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * takes arguments with folder paths and maps them to a relevant file and populator.
 * */
@Component
public class FilePopulateService {
    private static final Logger logger = LoggerFactory.getLogger(FilePopulateService.class);
    public static final String SEED_ROOT = "seed/";
    public static final String ABSOLUTE_SEED_ROOT = "populate/src/main/resources/seed/";

    private Environment environment;
    private boolean isPopulateFromClasspath;

    public FilePopulateService(Environment environment) {
        this.environment = environment;
        this.isPopulateFromClasspath = environment.getProperty("populate.populate-from-classpath", Boolean.class, true);
    }

    public String readFile(String relativePath, FilePopulateConfig popSpec) throws IOException {
        InputStream ios = getInputStream(relativePath, popSpec);
        String fileString = new String(ios.readAllBytes(), StandardCharsets.UTF_8);
        return fileString;
    }

    public byte[] readBinaryFile(String relativePath, FilePopulateConfig popSpec) throws IOException {
        InputStream ios = getInputStream(relativePath, popSpec);
        return ios.readAllBytes();
    }

    public InputStream getInputStream(String relativePath, FilePopulateConfig popSpec) throws IOException {
        if (isPopulateFromClasspath) {
            ClassPathResource cpr = new ClassPathResource(SEED_ROOT + popSpec.getBasePath() + "/" + relativePath);
            return cpr.getInputStream();
        }
        String pathName = System.getProperty("user.dir") + "/" + ABSOLUTE_SEED_ROOT + popSpec.getBasePath() + "/" + relativePath;
        Path filePath = Path.of(pathName);
        return Files.newInputStream(filePath);
    }

    public String readPopulationFile(FilePopulatable thing, FilePopulateConfig popSpec) throws IOException {
        if (thing.getPopulateFileName() != null) {
            return readFile(thing.getPopulateFileName(), popSpec);
        }
        return null;
    }

    public byte[] readBinaryPopulationFile(FilePopulatable thing, FilePopulateConfig popSpec) throws IOException {
        if (thing.getPopulateFileName() != null) {
            return readBinaryFile(thing.getPopulateFileName(), popSpec);
        }
        return null;
    }

}
