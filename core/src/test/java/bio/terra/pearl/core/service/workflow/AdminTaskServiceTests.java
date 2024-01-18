package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.AdminTask;
import bio.terra.pearl.core.model.workflow.DataAuditInfo;
import bio.terra.pearl.core.model.workflow.DataChangeRecord;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

public class AdminTaskServiceTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testAdminTaskCrud(TestInfo testInfo) {
        AdminUser user = adminUserFactory.buildPersisted(getTestName(testInfo));
        AdminUser user2 = adminUserFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(testInfo));

        AdminTask task = AdminTask.builder()
                .creatingAdminUserId(user.getId())
                .assignedAdminUserId(user2.getId())
                .studyEnvironmentId(studyEnvironment.getId())
                .description("some task")
                .build();
        AdminTask savedTask = adminTaskService.create(task, DataAuditInfo
                .builder()
                .responsibleAdminUserId(user.getId())
                .build());

        DaoTestUtils.assertGeneratedProperties(savedTask);
        assertThat(savedTask.getDescription(), equalTo(task.getDescription()));
        assertThat(savedTask.getStatus(), equalTo(TaskStatus.NEW));
        assertThat(adminTaskService.find(savedTask.getId()).get(), notNullValue());

        List<DataChangeRecord> changeRecords = dataChangeRecordService.findByModelId(savedTask.getId());
        assertThat(changeRecords.size(), equalTo(1));

        savedTask.setStatus(TaskStatus.COMPLETE);
        savedTask = adminTaskService.update(savedTask, DataAuditInfo
                .builder()
                .responsibleAdminUserId(user.getId())
                .build());
        assertThat(savedTask.getCompletedAt(), greaterThan(Instant.now().minusMillis(3000)));

        changeRecords = dataChangeRecordService.findByModelId(savedTask.getId());
        assertThat(changeRecords.size(), equalTo(2));

        adminTaskService.delete(savedTask.getId(), DataAuditInfo
                .builder()
                .responsibleAdminUserId(user.getId())
                .build());
        assertThat(adminTaskService.find(savedTask.getId()).isPresent(), equalTo(false));

        changeRecords = dataChangeRecordService.findByModelId(savedTask.getId());
        assertThat(changeRecords.size(), equalTo(3));
    }

    @Test
    @Transactional
    public void testAdminTaskRequiresStudyEnv(TestInfo testInfo) {
        AdminUser user = adminUserFactory.buildPersisted(getTestName(testInfo));
        AdminTask task = AdminTask.builder()
                .creatingAdminUserId(user.getId())
                .assignedAdminUserId(user.getId())
                .description("some task")
                .build();
        Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
            adminTaskService.create(task, DataAuditInfo
                    .builder()
                    .responsibleAdminUserId(user.getId())
                    .build());
        });
    }

    @Autowired
    private DataChangeRecordService dataChangeRecordService;
    @Autowired
    private AdminTaskService adminTaskService;
    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
}
