package bio.terra.pearl.populate.service.contexts;

import bio.terra.pearl.core.model.EnvironmentName;
import lombok.Getter;

@Getter
public class StudyPopulateContext extends PortalPopulateContext {
    private String studyShortcode;
    public StudyPopulateContext(String filePathName, String portalShortcode, String studyShortcode, EnvironmentName environmentName) {
        super(filePathName, portalShortcode, environmentName);
        this.studyShortcode = studyShortcode;
    }

    public StudyPopulateContext(PortalPopulateContext context, String studyShortcode) {
        this(context.getCurrentFile(), context.getPortalShortcode(), studyShortcode, context.getEnvironmentName());
    }

    public StudyPopulateContext newFrom(String relativeFilePath) {
        return new StudyPopulateContext(applyRelativePath(relativeFilePath), getPortalShortcode(), studyShortcode, getEnvironmentName());
    }

    public StudyPopulateContext newFrom(EnvironmentName environmentName) {
        return new StudyPopulateContext(getCurrentFile(), getPortalShortcode(), getStudyShortcode(), environmentName);
    }
}
