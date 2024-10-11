import TaskLink, { getTaskPath, isTaskAccessible, isTaskActive } from './TaskLink'
import { Link } from 'react-router-dom'
import React from 'react'
import { ParticipantTask } from 'api/api'
import { Enrollee, useI18n } from '@juniper/ui-core'


const taskTypeMap: Record<string, string> = {
  CONSENT: 'taskTypeConsent',
  SURVEY: 'taskTypeSurvey'
}

const enrolleeHasStartedTaskType = (enrollee: Enrollee, taskType: string): boolean => {
  return enrollee.participantTasks
    .filter(task => task.taskType === taskType && (task.status === 'COMPLETE' || task.status === 'IN_PROGRESS'))
    .length > 0
}

type StudyResearchTasksProps = {
  enrollee: Enrollee
  participantTasks: ParticipantTask[]
  studyShortcode: string
}

/** renders the research tasks (consents and research surveys) for the enrollee */
export default function StudyResearchTasks(props: StudyResearchTasksProps) {
  const { enrollee, studyShortcode, participantTasks } = props
  const { i18n } = useI18n()

  const hasStudyTasks = participantTasks.length > 0

  const sortedActiveConsentTasks = participantTasks
    .filter(task => task.taskType === 'CONSENT' && isTaskActive(task))
    .sort(taskComparator)
  const hasActiveConsentTasks = sortedActiveConsentTasks.length > 0

  const sortedSurveyTasks = participantTasks
    .filter(task => task.taskType === 'SURVEY')
    .sort(taskComparator)
  const hasSurveyTasks = sortedSurveyTasks.length > 0

  const nextTask = getNextTask(enrollee, [...sortedActiveConsentTasks, ...sortedSurveyTasks])
  const numTasksOfNextTaskType = nextTask
    ? enrollee.participantTasks.filter(task => task.taskType === nextTask.taskType).length
    : 0

  const completedConsentTasks = enrollee.participantTasks
    .filter(task => task.status === 'COMPLETE' && task.taskType === 'CONSENT')
  const hasCompletedConsentTasks = completedConsentTasks.length > 0

  if (!hasStudyTasks) {
    return <div className="fst-italic">{i18n('tasksNoneForStudy')}</div>
  }

  return (
    <>
      {nextTask && (
        <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
          <Link
            to={getTaskPath(nextTask, enrollee.shortcode, studyShortcode)}
            className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary"
          >
            {enrolleeHasStartedTaskType(enrollee, nextTask.taskType)
              ? i18n('continue')
              : i18n('start')}
            {' '}{i18n(taskTypeMap[nextTask.taskType])}
            {numTasksOfNextTaskType > 1 && 's'}
          </Link>
        </div>
      )}

      {hasActiveConsentTasks && (
        <TaskGrouping
          enrollee={enrollee}
          studyShortcode={studyShortcode}
          tasks={sortedActiveConsentTasks}
          title={i18n('taskTypeConsent')}
        />
      )}

      {hasSurveyTasks && (
        <TaskGrouping
          enrollee={enrollee}
          tasks={sortedSurveyTasks}
          studyShortcode={studyShortcode}
          title={i18n('taskTypeSurveys')}
        />
      )}

      {hasCompletedConsentTasks && (
        <TaskGrouping
          enrollee={enrollee}
          studyShortcode={studyShortcode}
          tasks={completedConsentTasks}
          title={i18n('taskTypeForms')}
        />
      )}
    </>
  )
}


/** renders a group like "CONSENTS" or "SURVEYS" */
function TaskGrouping({ title, tasks, enrollee, studyShortcode }: {
    title: string, tasks: ParticipantTask[],
    enrollee: Enrollee, studyShortcode: string
}) {
  const hasLockedTasks = tasks.some(task => !isTaskAccessible(task, enrollee))
  const { i18n } = useI18n()

  return (
    <>
      <h2 className="fs-6 text-uppercase mb-0">{title}</h2>
      {hasLockedTasks && (
        <p className="my-2 text-muted">{i18n('surveysSomeLocked')}</p>
      )}
      <ol className="list-unstyled p-0">
        {tasks.map(task => <li key={task.id}>
          <TaskLink task={task} key={task.id} studyShortcode={studyShortcode}
            enrollee={enrollee}/>
        </li>)}
      </ol>
    </>
  )
}

/** returns the next actionable task for the enrollee, or undefined if there is no remaining task */
export function getNextTask(enrollee: Enrollee, sortedTasks: ParticipantTask[]) {
  const nextTask = sortedTasks.find(task => isTaskAccessible(task, enrollee) && isTaskActive(task))
  return nextTask
}

export const TASK_TYPE_ORDER = ['CONSENT', 'SURVEY']
export const TASK_STATUS_ORDER = ['IN_PROGRESS', 'NEW', 'COMPLETE']

/** Sorts tasks based on their types, then based on status, and then based on their internal ordering */
export function taskComparator(taskA: ParticipantTask, taskB: ParticipantTask) {
  const typeOrder = TASK_TYPE_ORDER.indexOf(taskA.taskType) - TASK_TYPE_ORDER.indexOf(taskB.taskType)
  if (typeOrder != 0) {
    return typeOrder
  }
  const statusOrder = TASK_STATUS_ORDER.indexOf(taskA.status) - TASK_STATUS_ORDER.indexOf(taskB.status)
  if (statusOrder != 0) {
    return statusOrder
  }
  return taskA.taskOrder - taskB.taskOrder
}
