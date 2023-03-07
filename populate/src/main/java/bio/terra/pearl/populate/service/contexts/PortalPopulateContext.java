package bio.terra.pearl.populate.service.contexts;

import bio.terra.pearl.core.model.EnvironmentName;
import lombok.Getter;

@Getter
public class PortalPopulateContext extends FilePopulateContext {
    private String portalShortcode;
    private EnvironmentName environmentName;
    public PortalPopulateContext(String filePathName, String portalShortcode, EnvironmentName environmentName) {
        super(filePathName);
        this.environmentName = environmentName;
        this.portalShortcode = portalShortcode;
    }

    public PortalPopulateContext(FilePopulateContext context, String portalShortcode, EnvironmentName environmentName) {
        this(context.getCurrentFile(), portalShortcode, environmentName);
    }

    public PortalPopulateContext newFrom(String relativeFilePath) {
        return new PortalPopulateContext(applyRelativePath(relativeFilePath), portalShortcode, environmentName);
    }

    public PortalPopulateContext newFrom(EnvironmentName environmentName) {
        return new PortalPopulateContext(getCurrentFile(), portalShortcode, environmentName);
    }
}
