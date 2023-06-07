package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class KitRequestDaoTest extends BaseSpringBootTest {

    @Autowired
    private KitRequestDao kitRequestDao;

    @Transactional
    @Test
    public void testCreatSampleKit() {
        var adminUser = adminUserFactory.buildPersisted("testCreatSampleKit");
        var enrollee = enrolleeFactory.buildPersisted("testCreatSampleKit");
        var kitType = kitTypeFactory.buildPersisted("testCreatSampleKit");

        KitRequest kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitTypeId(kitType.getId())
                .sentToAddress("{ firstName:\"Alex\", lastName:\"Jones\", street1:\"123 Fake Street\" }")
                .status(KitRequestStatus.CREATED)
                .build();

        KitRequest savedKitRequest = kitRequestDao.create(kitRequest);

        DaoTestUtils.assertGeneratedProperties(savedKitRequest);
        assertThat(savedKitRequest.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(savedKitRequest.getEnrolleeId(), equalTo(enrollee.getId()));
        assertThat(savedKitRequest.getKitTypeId(), equalTo(kitType.getId()));
        assertThat(savedKitRequest, samePropertyValuesAs(kitRequest, "id", "createdAt", "lastUpdatedAt"));
        assertThat(savedKitRequest.getStatus(), equalTo(KitRequestStatus.CREATED));
    }

    @Transactional
    @Test
    public void testFindIncompleteKits() throws Exception {
        var adminUser = adminUserFactory.buildPersisted("testFindIncompleteKits");
        var studyEnvironment = studyEnvironmentFactory.buildPersisted("testUpdateAllKitStatuses");
        var newEnrollee = enrolleeFactory.buildPersisted("testFindIncompleteKits", studyEnvironment);
        var oldEnrollee = enrolleeFactory.buildPersisted("testFindIncompleteKits", studyEnvironment);
        var kitType = kitTypeFactory.buildPersisted("testFindIncompleteKits");

        var newKit = kitRequestFactory.builder("testFindIncompleteKits")
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(newEnrollee.getId())
                .kitTypeId(kitType.getId())
                .build();
        newKit = kitRequestDao.create(newKit);
        var completedKit = kitRequestFactory.builder("testFindIncompleteKits")
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(oldEnrollee.getId())
                .kitTypeId(kitType.getId())
                .status(KitRequestStatus.COMPLETE)
                .build();
        completedKit = kitRequestDao.create(completedKit);

        var incompleteKits = kitRequestDao.findIncompleteKits(studyEnvironment.getId());

        assertThat(incompleteKits, hasItem(newKit));
        assertThat(incompleteKits, not(hasItem(completedKit)));
    }

    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private KitTypeFactory kitTypeFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
}
