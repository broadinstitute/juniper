package bio.terra.pearl.populate.service;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@AllArgsConstructor
public class FilePopulateConfig {
    // current basePath we are populating from
    private String basePath;
    // the rootFile name that we started populating from
    private String rootFileName;

    public FilePopulateConfig(String filePathName) {
        Path filePath = Paths.get(filePathName);
        this.rootFileName = filePath.getFileName().toString();
        this.basePath = filePath.getParent().toString();
    }
}
