package bio.terra.pearl.populate.service.contexts;


import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;

/**
 * Stores the path and current filename to populate from.
 */
@Getter
public class FilePopulateContext {
    // current basePath we are populating from
    private String basePath;
    // the rootFile name that we started populating from
    private String rootFileName;


    public FilePopulateContext(String filePathName) {
        setPaths(filePathName);
    }

    protected void setPaths(String filePathName) {
        Path filePath = Paths.get(filePathName);
        this.rootFileName = filePath.getFileName().toString();
        this.basePath = filePath.getParent().toString();
    }

    public FilePopulateContext newFrom(String relativeFilePath) {
        return new FilePopulateContext(applyRelativePath(relativeFilePath));
    }

    protected String applyRelativePath(String relativeFilePath) {
        return getBasePath() + "/" + relativeFilePath;
    }

    public String getCurrentFile() {
        return getBasePath() + "/" + getRootFileName();
    }
}
