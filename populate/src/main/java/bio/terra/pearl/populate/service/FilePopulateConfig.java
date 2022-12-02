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

    private String portalShortcode;

    public FilePopulateConfig(String filePathName) {
        Path filePath = Paths.get(filePathName);
        this.rootFileName = filePath.getFileName().toString();
        this.basePath = filePath.getParent().toString();
    }

    public FilePopulateConfig newFrom(String relativeFilePath) {
        return newFrom(relativeFilePath, portalShortcode, studyShortcode, environmentName);
    }

    public FilePopulateConfig newFrom(String relativeFilePath,
                                      String portalShortcode,
                                      String studyShortcode,
                                      EnvironmentName environmentName) {
        FilePopulateConfig newConfig = new FilePopulateConfig(getBasePath() + "/" + relativeFilePath);
        newConfig.studyShortcode = studyShortcode;
        newConfig.environmentName = environmentName;
        newConfig.portalShortcode = portalShortcode;
        return newConfig;
    }

    public FilePopulateConfig newForStudy(String relativeFilePath,
                                      String studyShortcode,
                                      EnvironmentName environmentName) {
        return newFrom(relativeFilePath, portalShortcode, studyShortcode, environmentName);
    }

    public FilePopulateConfig newForPortal(String relativeFilePath,
                                          String portalShortcode,
                                          EnvironmentName environmentName) {
        return newFrom(relativeFilePath, portalShortcode, studyShortcode, environmentName);
    }
}
