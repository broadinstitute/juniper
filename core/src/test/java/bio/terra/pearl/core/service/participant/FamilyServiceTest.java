package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FamilyServiceTest extends BaseSpringBootTest {

    @Autowired
    private FamilyService familyService;

    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    private EnrolleeFactory enrolleeFactory;


    @Test
    @Transactional
    public void testCreateFamilyAddsShortcode(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Enrollee proband = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);

        Family created = familyService.create(
                Family.builder()
                        .studyEnvironmentId(studyEnvironment.getId())
                        .probandEnrolleeId(proband.getId())
                        .build(),
                getAuditInfo(info));

        assertNotNull(created.getShortcode());
        assertTrue(created.getShortcode().startsWith("F_"));
    }
}
