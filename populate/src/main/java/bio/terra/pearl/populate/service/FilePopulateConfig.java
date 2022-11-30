package bio.terra.pearl.populate.service;


import bio.terra.pearl.core.model.EnvironmentName;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Stores the path and current filename to populate from.  Also tracks any contextual information, such as the current
 * study that entities may need for creation.
 */
@Getter
public class FilePopulateConfig {
    // current basePath we are populating from
    private String basePath;
    // the rootFile name that we started populating from
    private String rootFileName;

    private String studyShortcode;

    private EnvironmentName environmentName;

    public FilePopulateConfig(String filePathName) {
        Path filePath = Paths.get(filePathName);
        this.rootFileName = filePath.getFileName().toString();
        this.basePath = filePath.getParent().toString();
    }

    public FilePopulateConfig newFrom(String relativeFilePath) {
        return newFrom(relativeFilePath, studyShortcode, environmentName);
    }

    public FilePopulateConfig newFrom(String relativeFilePath, String studyShortcode, EnvironmentName environmentName) {
        FilePopulateConfig newConfig = new FilePopulateConfig(getBasePath() + "/" + relativeFilePath);
        newConfig.studyShortcode = studyShortcode;
        newConfig.environmentName = environmentName;
        return newConfig;
    }
}
