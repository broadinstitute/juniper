package bio.terra.pearl.populate.service.contexts;

import bio.terra.pearl.core.model.EnvironmentName;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class PortalPopulateContext extends FilePopulateContext {
    private String portalShortcode;
    private EnvironmentName environmentName;
    public PortalPopulateContext(String filePathName, String portalShortcode, EnvironmentName environmentName,
                                 Map<String, UUID> populatedFileEntities, boolean isFromTempDir) {
        super(filePathName);
        this.populatedFileEntities = populatedFileEntities;
        this.environmentName = environmentName;
        this.portalShortcode = portalShortcode;
        this.isFromTempDir = isFromTempDir;
    }

    public PortalPopulateContext(FilePopulateContext context, String portalShortcode, EnvironmentName environmentName) {
        this(context.getCurrentFile(), portalShortcode, environmentName, context.populatedFileEntities, context.isFromTempDir);
    }

    public PortalPopulateContext newFrom(String relativeFilePath) {
        return new PortalPopulateContext(applyRelativePath(relativeFilePath), portalShortcode, environmentName,
                populatedFileEntities, isFromTempDir);
    }

    public PortalPopulateContext newFrom(EnvironmentName environmentName) {
        return new PortalPopulateContext(getCurrentFile(), portalShortcode, environmentName, populatedFileEntities, isFromTempDir);
    }
}
