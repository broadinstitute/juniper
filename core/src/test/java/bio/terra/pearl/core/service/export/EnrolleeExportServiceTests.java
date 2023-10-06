package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class EnrolleeExportServiceTests extends BaseSpringBootTest {
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private EnrolleeExportService enrolleeExportService;

    @Test
    public void testExportNumberLimit(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(testName);
        Enrollee enrollee1 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(testName, studyEnv, new Profile());
        var exportModuleInfo = enrolleeExportService.generateModuleInfos(new ExportOptions(), studyEnv.getId());
        List<Map<String, String>> exportMaps = enrolleeExportService.generateExportMaps(studyEnv.getId(), exportModuleInfo, 2);

        assertThat(exportMaps, hasSize(2));
        // confirm enrollees are in reverse order of creation
        assertThat(exportMaps.get(0).get("enrollee.shortcode"), equalTo(enrollee3.getShortcode()));
        assertThat(exportMaps.get(1).get("enrollee.shortcode"), equalTo(enrollee2.getShortcode()));
    }
}
