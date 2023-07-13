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
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
    public void testFindByStatus() throws Exception {
        // Arrange
        var adminUser = adminUserFactory.buildPersisted("testFindIncompleteKits");
        var studyEnvironment = studyEnvironmentFactory.buildPersisted("testUpdateAllKitStatuses");
        var enrollee = enrolleeFactory.buildPersisted("testFindIncompleteKits", studyEnvironment);
        var kitType = kitTypeFactory.buildPersisted("testFindIncompleteKits");

        Function<KitRequestStatus, KitRequest> makeKit = status -> {
            try {
                var kit = kitRequestFactory.builder("testFindIncompleteKits " + status.name())
                        .creatingAdminUserId(adminUser.getId())
                        .enrolleeId(enrollee.getId())
                        .kitTypeId(kitType.getId())
                        .status(status)
                        .build();
                return kitRequestDao.create(kit);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
        var incompleteKits = Stream.of(KitRequestStatus.CREATED, KitRequestStatus.IN_PROGRESS).map(makeKit).toList();
        var completeKits = Stream.of(KitRequestStatus.COMPLETE, KitRequestStatus.FAILED).map(makeKit).toList();

        // Act
        var fetchedIncompleteKits = kitRequestDao.findByStatus(
                studyEnvironment.getId(),
                List.of(KitRequestStatus.CREATED, KitRequestStatus.IN_PROGRESS));
        var fetchedCompleteKits = kitRequestDao.findByStatus(
                studyEnvironment.getId(),
                List.of(KitRequestStatus.COMPLETE, KitRequestStatus.FAILED));

        // Assert
        assertThat(fetchedIncompleteKits, containsInAnyOrder(incompleteKits.toArray()));
        assertThat(fetchedCompleteKits, containsInAnyOrder(completeKits.toArray()));
    }

    @Transactional
    @Test
    public void testFindByStudyEnvironment() throws Exception {
        var adminUser = adminUserFactory.buildPersisted("testFindByStudyEnvironment");
        var kitType = kitTypeFactory.buildPersisted("testFindByStudyEnvironment");
        var studyEnvironment1 = studyEnvironmentFactory.buildPersisted("testFindByStudyEnvironment 1");
        var studyEnvironment2 = studyEnvironmentFactory.buildPersisted("testFindByStudyEnvironment 2");
        var enrollee1 = enrolleeFactory.buildPersisted("testFindByStudyEnvironment 1", studyEnvironment1);
        var enrollee2 = enrolleeFactory.buildPersisted("testFindByStudyEnvironment 2", studyEnvironment2);

        var kit1 = kitRequestFactory.builder("testFindByStudyEnvironment 1")
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee1.getId())
                .kitTypeId(kitType.getId())
                .build();
        kit1 = kitRequestDao.create(kit1);
        var kit2 = kitRequestFactory.builder("testFindByStudyEnvironment 2")
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee2.getId())
                .kitTypeId(kitType.getId())
                .build();
        kit2 = kitRequestDao.create(kit2);

        var kits1 = kitRequestDao.findByStudyEnvironment(studyEnvironment1.getId());
        assertThat(kits1, contains(kit1));

        var kits2 = kitRequestDao.findByStudyEnvironment(studyEnvironment2.getId());
        assertThat(kits2, contains(kit2));
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
