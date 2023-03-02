package bio.terra.pearl.populate.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * takes arguments with folder paths and maps them to a relevant file and populator.
 * */
@Component
public class FilePopulateService {
    private static final Logger logger = LoggerFactory.getLogger(FilePopulateService.class);
    public static final String SEED_ROOT = "seed/";
    public static final String ABSOLUTE_SEED_ROOT = "populate/src/main/resources/seed/";

    // Whether to read files from the classpath or from local directory structure
    private boolean isPopulateFromClasspath;

    public FilePopulateService(Environment environment) {
        this.isPopulateFromClasspath = environment
                .getProperty("env.populate.populate-from-classpath", Boolean.class, true);
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
        if (relativePath.contains("..") || popSpec.getBasePath().contains("..")) {
            throw new IllegalArgumentException("'..' is not permitted in paths to be read");
        }
        if (isPopulateFromClasspath) {
            ClassPathResource cpr = new ClassPathResource(SEED_ROOT + popSpec.getBasePath() + "/" + relativePath);
            return cpr.getInputStream();
        }
        /**
         * depending on whether you are running gradle or spring boot, the root directory could either be
         * the root folder or api-admin.  So strip out api-admin if it's there
         */
        String projectDir = System.getProperty("user.dir").replace("/api-admin", "");
        String pathName = projectDir + "/" + ABSOLUTE_SEED_ROOT + popSpec.getBasePath() + "/" + relativePath;
        Path filePath = Path.of(pathName);
        return Files.newInputStream(filePath);
    }
}
