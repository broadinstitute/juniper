package bio.terra.pearl.core.service.dataimport;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportStatus;
import bio.terra.pearl.core.model.dataimport.ImportType;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.export.dataimport.ImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ImportServiceTest extends BaseSpringBootTest {
    @Autowired
    private ImportService importService;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;

    @Test
    @Transactional
    public void testCrud(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info));
        Import dataImport = Import.builder()
                .responsibleUserId(user.getId())
                .studyEnvironmentId(bundle.getStudyEnv().getId())
                .importType(ImportType.PARTICIPANT)
                .status(ImportStatus.PROCESSING)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();
        Import savedImport = importService.create(dataImport);
        DaoTestUtils.assertGeneratedProperties(savedImport);

        Optional<Import> foundImport = importService.find(savedImport.getId());
        assertThat(foundImport.get().getStatus(), equalTo(dataImport.getStatus()));

        importService.delete(savedImport.getId(), CascadeProperty.EMPTY_SET);
        assertThat(importService.find(savedImport.getId()).isEmpty(), equalTo(true));
    }

}
