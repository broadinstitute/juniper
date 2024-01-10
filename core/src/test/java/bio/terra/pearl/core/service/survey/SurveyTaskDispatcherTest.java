package bio.terra.pearl.core.service.survey;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import org.junit.jupiter.api.Test;

class SurveyTaskDispatcherTest extends BaseSpringBootTest {

    @Test
    void testIsDuplicateTask() {
        StudyEnvironmentSurvey studyEnvironmentSurvey = StudyEnvironmentSurvey.builder()
                .recur(false)
                .build();
        ParticipantTask surveyTask1 = ParticipantTask.builder()
                .targetStableId("TASK_1")
                .taskType(TaskType.SURVEY)
                .build();
        ParticipantTask surveyTask2 = ParticipantTask.builder()
                .targetStableId("TASK_2")
                .taskType(TaskType.SURVEY)
                .build();
        ParticipantTask kitTask = ParticipantTask.builder()
                .taskType(TaskType.KIT_REQUEST)
                .build();
        Set<ParticipantTask> existingTasks = Set.of(surveyTask1, surveyTask2, kitTask);
        boolean isDuplicate = SurveyTaskDispatcher.isDuplicateTask(studyEnvironmentSurvey, surveyTask1,
                existingTasks);
        assertTrue(isDuplicate);

        ParticipantTask surveyTask3 = ParticipantTask.builder()
                .targetStableId("TASK_3")
                .taskType(TaskType.SURVEY)
                .build();
        isDuplicate = SurveyTaskDispatcher.isDuplicateTask(studyEnvironmentSurvey, surveyTask3,
                existingTasks);
        assertFalse(isDuplicate);
    }

    @Test
    void testOutreachIsDuplicate() {
        StudyEnvironmentSurvey studyEnvironmentSurvey = StudyEnvironmentSurvey.builder()
                .recur(false)
                .build();
        ParticipantTask outreachTask1 = ParticipantTask.builder()
                .targetStableId("oh_outsideAdvert")
                .taskType(TaskType.OUTREACH)
                .build();
        Set<ParticipantTask> existingTasks = Set.of(outreachTask1);
        ParticipantTask outreachTask2 = ParticipantTask.builder()
                .targetStableId("oh_outsideAdvert")
                .taskType(TaskType.OUTREACH)
                .build();
        boolean isDuplicate = SurveyTaskDispatcher.isDuplicateTask(studyEnvironmentSurvey, outreachTask2,
                existingTasks);
        assertTrue(isDuplicate);
    }
}
