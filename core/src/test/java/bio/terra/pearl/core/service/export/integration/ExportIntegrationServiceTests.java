package bio.terra.pearl.core.service.export.integration;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.export.ExportDestinationType;
import bio.terra.pearl.core.model.export.ExportIntegration;
import bio.terra.pearl.core.model.export.ExportIntegrationJob;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.ExportOptionsWithExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ExportIntegrationServiceTests extends BaseSpringBootTest {
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private ExportIntegrationService exportIntegrationService;
    @Autowired
    private ExportIntegrationJobService exportIntegrationJobService;
    @Autowired
    private EnrolleeExportService enrolleeExportService;


    @Test
    @Transactional
    public void testExternalExportJobCreation(TestInfo testInfo) throws InterruptedException {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        ExportOptionsWithExpression opts = ExportOptionsWithExpression.builder().rowLimit(2).build();

        ExportIntegration exportIntegration = exportIntegrationService.create(ExportIntegration.builder()
                        .name(getTestName(testInfo))
                .studyEnvironmentId(studyEnv.getId())
                .enabled(true)
                .exportOptions(opts)
                .destinationType(ExportDestinationType.AIRTABLE)
                .destinationUrl("badURL")
                .build());
        ExportIntegration loadedIntegration = exportIntegrationService.findWithOptions(exportIntegration.getId()).get();

        assertThat(exportIntegrationJobService.findByStudyEnvironment(studyEnv.getId()), hasSize(0));
        exportIntegrationService.doExport(new MockExporter(), loadedIntegration, new ResponsibleEntity("testExternalExportJobCreation"));
        Thread.sleep(200); // quick-and-dirty way to wait until the async op completes
        List<ExportIntegrationJob> jobs = exportIntegrationJobService.findByStudyEnvironment(studyEnv.getId());
        assertThat(jobs, hasSize(1));
        assertThat(jobs.get(0).getExportIntegrationId(), equalTo(exportIntegration.getId()));
        assertThat(jobs.get(0).getStatus(), equalTo(ExportIntegrationJob.Status.COMPLETE));
        assertThat(jobs.get(0).getResult(), containsString("mock done"));
    }

    @Test
    @Transactional
    public void testExternalExportJobError(TestInfo testInfo) throws InterruptedException {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());

        ExportOptionsWithExpression opts = ExportOptionsWithExpression.builder().rowLimit(2).build();

        ExportIntegration exportIntegration = exportIntegrationService.create(ExportIntegration.builder()
                .name(getTestName(testInfo))
                .studyEnvironmentId(studyEnv.getId())
                .enabled(true)
                .exportOptions(opts)
                .destinationType(ExportDestinationType.AIRTABLE)
                .destinationUrl("badURL")
                .build());
        ExportIntegration loadedIntegration = exportIntegrationService.findWithOptions(exportIntegration.getId()).get();

        assertThat(exportIntegrationJobService.findByStudyEnvironment(studyEnv.getId()), hasSize(0));
        exportIntegrationService.doExport(new MockErrorExporter(), loadedIntegration, new ResponsibleEntity("testExternalExportJobError"));
        Thread.sleep(200); // quick-and-dirty way to wait until the async op completes
        List<ExportIntegrationJob> jobs = exportIntegrationJobService.findByStudyEnvironment(studyEnv.getId());
        assertThat(jobs, hasSize(1));
        assertThat(jobs.get(0).getExportIntegrationId(), equalTo(exportIntegration.getId()));
        assertThat(jobs.get(0).getStatus(), equalTo(ExportIntegrationJob.Status.FAILED));
        assertThat(jobs.get(0).getResult(), containsString("mock error"));
    }

    protected class MockExporter extends ExternalExporter {
        public MockExporter() {
            super(exportIntegrationJobService, enrolleeExportService);
        }
        @Override
        public void send(ExportIntegration integration, ByteArrayOutputStream outputStream, Consumer<String> handleComplete, Consumer<Exception> handleError) {
            // confirm the output stream can be stringified, otherwise do nothing
            outputStream.toString();
            handleComplete.accept("mock done");
        }
    }

    protected class MockErrorExporter extends ExternalExporter {
        public MockErrorExporter() {
            super(exportIntegrationJobService, enrolleeExportService);
        }
        @Override
        public void send(ExportIntegration integration, ByteArrayOutputStream outputStream, Consumer<String> handleComplete, Consumer<Exception> handleError) {
            handleError.accept(new RuntimeException("mock error"));
        }
    }
}
