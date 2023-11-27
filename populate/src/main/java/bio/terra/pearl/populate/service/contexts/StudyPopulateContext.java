package bio.terra.pearl.populate.service.contexts;

import bio.terra.pearl.core.model.EnvironmentName;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class StudyPopulateContext extends PortalPopulateContext {
    private String studyShortcode;
    public StudyPopulateContext(String filePathName, String portalShortcode, String studyShortcode, EnvironmentName environmentName,
                                Map<String, UUID> populatedFileEntities, boolean isFromTempDir) {
        super(filePathName, portalShortcode, environmentName, populatedFileEntities, isFromTempDir);
        this.studyShortcode = studyShortcode;
    }

    public StudyPopulateContext(PortalPopulateContext context, String studyShortcode) {
        this(context.getCurrentFile(), context.getPortalShortcode(), studyShortcode, context.getEnvironmentName(),
                context.populatedFileEntities, context.isFromTempDir);
    }

    public StudyPopulateContext newFrom(String relativeFilePath) {
        return new StudyPopulateContext(applyRelativePath(relativeFilePath), getPortalShortcode(), studyShortcode,
                getEnvironmentName(), populatedFileEntities, isFromTempDir);
    }

    public StudyPopulateContext newFrom(EnvironmentName environmentName) {
        return new StudyPopulateContext(getCurrentFile(), getPortalShortcode(), getStudyShortcode(), environmentName,
                populatedFileEntities, isFromTempDir);
    }
}
