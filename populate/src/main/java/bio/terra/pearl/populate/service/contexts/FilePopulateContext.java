package bio.terra.pearl.populate.service.contexts;


import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.populate.dto.FilePopulatable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;

/**
 * Stores the path and current filename to populate from.
 * Also stores version information for any documents that needed to be saved as different versions than in their
 * populate files, to avoid overwriting existing versions
 */
@Getter
public class FilePopulateContext {
    // current basePath we are populating from
    private String basePath;
    // the rootFile name that we started populating from
    private String rootFileName;
    /** maps file names to UUIDs of the entity populated from them */
    protected Map<String, UUID> populatedFileEntities = new HashMap<>();

    public FilePopulateContext(String filePathName) {
        setPaths(filePathName);
    }

    protected void setPaths(String filePathName) {
        Path filePath = Paths.get(filePathName);
        this.rootFileName = filePath.getFileName().toString();
        this.basePath = filePath.getParent().toString();
    }

    public FilePopulateContext newFrom(String relativeFilePath) {
        var popContext = new FilePopulateContext(applyRelativePath(relativeFilePath));
        popContext.populatedFileEntities = this.populatedFileEntities;
        return popContext;
    }

    protected String applyRelativePath(String relativeFilePath) {
        return getBasePath() + "/" + relativeFilePath;
    }

    public String getCurrentFile() {
        return getBasePath() + "/" + getRootFileName();
    }

    public UUID getUUIDForFileName(String fileName) {
        return populatedFileEntities.get(fileName);
    }

    public void markFilenameAsPopulated(String fileName, UUID entity) {
        populatedFileEntities.put(fileName, entity);
    }

    /**
     * checks whether something has already been populated in this context -- to avoid creating, e.g., multiple
     * copies of a survey in the same populate run
     */
    public boolean isAlreadyPopulated(String filename) {
        return populatedFileEntities.containsKey(filename);
    }

    public <T extends BaseEntity> Optional<T> fetchFromPopDto(FilePopulatable popDto, ImmutableEntityService<T, ?> service) {
        if (popDto.getPopulateFileName() != null) {
            String fullName = getBasePath() + "/" + popDto.getPopulateFileName();
            if (isAlreadyPopulated(fullName)) {
                return service.find(getUUIDForFileName(fullName));
            }
        }
        return Optional.empty();
    }

}
