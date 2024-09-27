package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.export.integration.ExportIntegrationService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class ExportIntegrationPopulator extends BasePopulator<ExportIntegration, ExportIntegration, StudyPopulateContext> {

    private final ExportIntegrationService exportIntegrationService;
    private final StudyEnvironmentService studyEnvironmentService;

    public ExportIntegrationPopulator(ExportIntegrationService exportIntegrationService, StudyEnvironmentService studyEnvironmentService, EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        this.exportIntegrationService = exportIntegrationService;
        this.studyEnvironmentService = studyEnvironmentService;
    }

    @Override
    protected void preProcessDto(ExportIntegration popDto, StudyPopulateContext context) throws IOException {
        StudyEnvironment studyEnvironment = studyEnvironmentService.findByStudy(context.getStudyShortcode(), context.getEnvironmentName())
                .orElseThrow(() -> new IllegalArgumentException("Study environment not found"));
        popDto.setStudyEnvironmentId(studyEnvironment.getId());
    }

    @Override
    protected Class<ExportIntegration> getDtoClazz() {
        return ExportIntegration.class;
    }

    @Override
    public Optional<ExportIntegration> findFromDto(ExportIntegration popDto, StudyPopulateContext context) {
        // export integrations are not uniquely identified
        return Optional.empty();
    }

    @Override
    public ExportIntegration overwriteExisting(ExportIntegration existingObj, ExportIntegration popDto, StudyPopulateContext context) throws IOException {
        exportIntegrationService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public ExportIntegration createPreserveExisting(ExportIntegration existingObj, ExportIntegration popDto, StudyPopulateContext context) throws IOException {
        return createNew(popDto, context, true);
    }

    @Override
    public ExportIntegration createNew(ExportIntegration popDto, StudyPopulateContext context, boolean overwrite) throws IOException {
        return exportIntegrationService.create(popDto);
    }
}
