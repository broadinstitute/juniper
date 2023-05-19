package bio.terra.pearl.core.dao.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class KitRequestDaoTest extends BaseSpringBootTest {

    @Autowired
    private KitRequestDao kitRequestDao;

    @Transactional
    @Test
    public void testCreatSampleKit() {
        var adminUser = adminUserFactory.buildPersisted("testCreatSampleKit");
        var enrollee = enrolleeFactory.buildPersisted("testCreatSampleKit");

        KitRequest kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUser.getId())
                .enrolleeId(enrollee.getId())
                .kitType("blood")
                .sentToAddress("{ firstName:\"Alex\", lastName:\"Jones\", street1:\"123 Fake Street\" }")
                .status(KitRequestStatus.CREATED)
                .build();

        KitRequest savedKitRequest = kitRequestDao.create(kitRequest);

        DaoTestUtils.assertGeneratedProperties(savedKitRequest);
        assertThat(savedKitRequest.getCreatingAdminUserId(), equalTo(adminUser.getId()));
        assertThat(savedKitRequest.getEnrolleeId(), equalTo(enrollee.getId()));
        assertThat(savedKitRequest.getKitType(), equalTo("blood"));
        assertThat(savedKitRequest, samePropertyValuesAs(kitRequest, "id", "createdAt", "lastUpdatedAt"));
        assertThat(savedKitRequest.getStatus(), equalTo(KitRequestStatus.CREATED));
    }

    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
}
