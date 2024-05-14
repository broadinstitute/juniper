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
import org.apache.commons.lang3.StringUtils;

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
    /** whether this context maps to the populate seed directory, or the temp directory */
    protected boolean isFromTempDir = false;
    protected String shortcodeOverride = null;

    public FilePopulateContext(String filePathName) {
        setPaths(filePathName);
    }

    public FilePopulateContext(String filePathName, boolean isFromTempDir) {
        this(filePathName);
        this.isFromTempDir = isFromTempDir;
    }

    public FilePopulateContext(String filePathName, boolean isFromTempDir, String shortcodeOverride) {
        this(filePathName, isFromTempDir);
        this.shortcodeOverride = shortcodeOverride;
    }


    protected void setPaths(String filePathName) {
        if (filePathName == null) {
            return;
        }
        Path filePath = Paths.get(filePathName);
        this.rootFileName = filePath.getFileName().toString();
        this.basePath = filePath.getParent().toString();
    }

    public FilePopulateContext newFrom(String relativeFilePath) {
        FilePopulateContext popContext = new FilePopulateContext(applyRelativePath(relativeFilePath));
        popContext.populatedFileEntities = this.populatedFileEntities;
        popContext.isFromTempDir = this.isFromTempDir;
        popContext.shortcodeOverride = this.shortcodeOverride;
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

    public void markFilenameAsPopulated(String filename, UUID entity) {
        String normalizedName = Paths.get(filename).normalize().toString();
        populatedFileEntities.put(normalizedName, entity);
    }

    /**
     * checks whether something has already been populated in this context -- to avoid creating, e.g., multiple
     * copies of a survey in the same populate run
     */
    public boolean isAlreadyPopulated(String filename) {
        String normalizedName = Paths.get(filename).normalize().toString();
        return populatedFileEntities.containsKey(normalizedName);
    }

    public <T extends BaseEntity> Optional<T> fetchFromPopDto(FilePopulatable popDto, ImmutableEntityService<T, ?> service) {
        if (popDto.getPopulateFileName() != null) {
            String fullName = getBasePath() + "/" + popDto.getPopulateFileName();
            String normalizedName = Paths.get(fullName).normalize().toString();
            if (isAlreadyPopulated(normalizedName)) {
                return service.find(getUUIDForFileName(normalizedName));
            }
        }
        return Optional.empty();
    }

    public String applyShortcodeOverride(String stableId) {
        if (!StringUtils.isBlank(shortcodeOverride)) {
            String newStableId = stableId;
            // if the stableId is already prefixed, strip it
            if (stableId.lastIndexOf("_") != -1) {
                newStableId = stableId.substring(stableId.lastIndexOf("_") + 1);
            }
            return shortcodeOverride + "_" + newStableId;
        }
        return stableId;
    }

}
