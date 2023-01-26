package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class EnrolleeServiceTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private EnrolleeService enrolleeService;

    @Test
    @Transactional
    public void testEnrolleeCreate() {
        Enrollee enrollee = enrolleeFactory.builderWithDependencies("testEnrolleeCrud").build();
        Enrollee savedEnrollee = enrolleeService.create(enrollee);
        Assertions.assertNotNull(savedEnrollee.getId());
        Assertions.assertNotNull(savedEnrollee.getShortcode());
        Assertions.assertEquals(enrollee.getParticipantUserId(), enrollee.getParticipantUserId());
    }
}
