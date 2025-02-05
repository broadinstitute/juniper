import { Link } from 'react-router-dom'
import React from 'react'
import { ParticipantTask } from 'api/api'
import { Enrollee, useI18n } from '@juniper/ui-core'
import { getNextTask, getTaskPath, isTaskAccessible, isTaskActive, taskComparator } from './task/taskUtils'
import TaskLink from './TaskLink'
import _groupBy from 'lodash/groupBy'
import _flatten from 'lodash/flatten'

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
  const viewableParticipantTasks = participantTasks.filter(task => task.status !== 'REMOVED')

  const activeConsentTaskGroups =  groupAndSortTasks(viewableParticipantTasks.filter(task =>
    task.taskType === 'CONSENT' && isTaskActive(task)))

  const sortedSurveyTaskGroups = groupAndSortTasks(viewableParticipantTasks.filter(task =>
    task.taskType === 'SURVEY'))

  const sortedDocumentRequestGroups = groupAndSortTasks(viewableParticipantTasks.filter(task =>
    task.taskType === 'DOCUMENT_REQUEST'))

  const sortedCurrentTasks = [...activeConsentTaskGroups.map(group => group[0]),
    ...sortedSurveyTaskGroups.map(group => group[0])]
  const nextTask = getNextTask(enrollee, sortedCurrentTasks)
  const numTasksOfNextTaskType = nextTask
    ? viewableParticipantTasks.filter(task => task.taskType === nextTask.taskType).length
    : 0

  const completedConsentTaskGroups = groupAndSortTasks(viewableParticipantTasks.filter(task =>
    task.taskType === 'CONSENT' && task.status === 'COMPLETE'))

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

      <TaskGrouping
        enrollee={enrollee}
        studyShortcode={studyShortcode}
        taskArrays={activeConsentTaskGroups}
        title={i18n('taskTypeConsent')}
      />
      <TaskGrouping
        enrollee={enrollee}
        taskArrays={sortedSurveyTaskGroups}
        studyShortcode={studyShortcode}
        title={i18n('taskTypeSurveys')}
      />
      <TaskGrouping
        enrollee={enrollee}
        studyShortcode={studyShortcode}
        taskArrays={sortedDocumentRequestGroups}
        title={i18n('taskTypeDocumentRequests')}
      />
      <TaskGrouping
        enrollee={enrollee}
        studyShortcode={studyShortcode}
        taskArrays={completedConsentTaskGroups}
        title={i18n('taskTypeForms')}
      />
    </>
  )
}

/** groups and sorts tasks by targetStableId.  within each group, tasks are sorted by recency.
 * The array of groups returned is sorted by the taskComparator sort across the most recent task in each group. */
function groupAndSortTasks(tasks: ParticipantTask[]) {
  const surveyTaskGroups = Object.values(_groupBy(tasks, 'targetStableId'))
  surveyTaskGroups.forEach(tasks =>
    tasks.sort((a, b) => b.createdAt - a.createdAt))
  return surveyTaskGroups
    .sort((a, b) => taskComparator(a[0], b[0]))
}


/** renders a group like "CONSENTS" or "SURVEYS" */
function TaskGrouping({ title, taskArrays, enrollee, studyShortcode }: {
    title: string, taskArrays: ParticipantTask[][],
    enrollee: Enrollee, studyShortcode: string
}) {
  const { i18n } = useI18n()
  if (taskArrays.length === 0) {
    return null
  }
  const hasLockedTasks = _flatten(taskArrays).some(task => !isTaskAccessible(task, enrollee))
  return (
    <>
      <h2 className="fs-6 text-uppercase mb-0">{title}</h2>
      {hasLockedTasks && (
        <p className="my-2 text-muted">{i18n('surveysSomeLocked')}</p>
      )}
      <ol className="list-unstyled p-0">
        {taskArrays.map(tasks =>
          <TaskLink task={tasks[0]} history={tasks.slice(1)}
            enrollee={enrollee} studyShortcode={studyShortcode} key={tasks[0].id}/>
        )}
      </ol>
    </>
  )
}
