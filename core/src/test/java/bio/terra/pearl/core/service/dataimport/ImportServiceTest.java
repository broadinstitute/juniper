package bio.terra.pearl.core.service.dataimport;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dataimport.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.export.dataimport.ImportItemService;
import bio.terra.pearl.core.service.export.dataimport.ImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImportServiceTest extends BaseSpringBootTest {
    @Autowired
    private ImportService importService;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private ImportItemService importItemService;

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

    @Test
    @Transactional
    public void testAttachItems(TestInfo info) {
        StudyEnvironmentBundle bundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.irb);
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info));

        Enrollee enrollee1 = enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv());
        Enrollee enrollee2 = enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv());
        Enrollee enrollee3 = enrolleeFactory.buildPersisted(getTestName(info), bundle.getStudyEnv());

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


        ImportItem item1 = ImportItem.builder()
                .createdEnrolleeId(enrollee1.getId())
                .importId(savedImport.getId())
                .status(ImportItemStatus.SUCCESS)
                .message("Item 1")
                .build();

        ImportItem item2 = ImportItem.builder()
                .createdEnrolleeId(enrollee2.getId())
                .importId(savedImport.getId())
                .status(ImportItemStatus.SUCCESS)
                .message("Item 2")
                .build();

        ImportItem item3 = ImportItem.builder()
                .createdEnrolleeId(enrollee3.getId())
                .importId(savedImport.getId())
                .status(ImportItemStatus.SUCCESS)
                .message("Item 3")
                .build();

        ImportItem item4 = ImportItem.builder()
                .createdEnrolleeId(null) // This item has no enrollee
                .importId(savedImport.getId())
                .status(ImportItemStatus.FAILED)
                .message("Item 4")
                .build();

        importItemService.create(item1);
        importItemService.create(item2);
        importItemService.create(item3);
        importItemService.create(item4);

        importItemService.attachImportItems(savedImport);

        assertEquals(4, savedImport.getImportItems().size());

        assertEquals(3, savedImport.getImportItems().stream()
                .filter(item -> item.getCreatedEnrolleeId() != null)
                .count());

        assertTrue(savedImport.getImportItems().stream()
                .filter(item -> item.getCreatedEnrolleeId() != null)
                .allMatch(item -> item.getCreatedEnrolleeId().equals(item.getCreatedEnrollee().getId())));
    }

}
