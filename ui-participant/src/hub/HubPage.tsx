import React, { useState } from 'react'
import { usePortalEnv } from '../providers/PortalProvider'
import { useUser } from '../providers/UserProvider'

import { Enrollee, ParticipantTask, Portal, Study } from '../api/api'
import TaskLink, { getTaskPath, isTaskAccessible, isTaskActive } from './TaskLink'
import { Link, NavLink } from 'react-router-dom'
import { DocumentTitle } from 'util/DocumentTitle'
import { userHasJoinedPortalStudy } from 'util/enrolleeUtils'

import { HubMessageAlert, useHubUpdate } from './hubUpdates'


/** renders the logged-in hub page */
export default function HubPage() {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()

  const hubUpdate = useHubUpdate()
  const [displayedHubMessage, setDisplayedHubMessage] = useState(hubUpdate?.message)

  const unjoinedStudies = portal.portalStudies.filter(pStudy => !userHasJoinedPortalStudy(pStudy, enrollees))
  const hasUnjoinedStudies = unjoinedStudies.length > 0
  return (
    <>
      <DocumentTitle title="Dashboard" />
      <div
        className="hub-dashboard-background flex-grow-1"
        style={{ background: 'linear-gradient(270deg, #D5ADCC 0%, #E5D7C3 100%' }}
      >
        {!!displayedHubMessage && (
          <HubMessageAlert
            message={displayedHubMessage}
            className="mx-1 mx-md-auto my-1 my-md-5 shadow-sm"
            role="alert"
            style={{ maxWidth: 768 }}
            onDismiss={() => {
              setDisplayedHubMessage(undefined)
            }}
          />
        )}

        <main
          className="hub-dashboard py-4 px-2 px-md-5 my-md-5 mx-auto shadow-sm"
          style={{ background: '#fff', maxWidth: 768 }}
        >
          {enrollees.map(enrollee => <StudySection key={enrollee.id} enrollee={enrollee} portal={portal} />)}

          {hasUnjoinedStudies && (
            <>
              <h1 className="text-center">Studies you can join</h1>
              <ul className="list-group">
                {unjoinedStudies.map(portalStudy => <li key={portalStudy.study.shortcode} className="list-group-item">
                  <h6>{portalStudy.study.name}</h6>
                  <NavLink to={`/studies/${portalStudy.study.shortcode}/join`}>Join</NavLink>
                </li>)}
              </ul>
            </>
          )}
        </main>
      </div>
    </>
  )
}

type StudySectionProps = {
  enrollee: Enrollee
  portal: Portal
}

const StudySection = (props: StudySectionProps) => {
  const { enrollee, portal } = props

  const matchedStudy = portal.portalStudies
    .find(pStudy => pStudy.study.studyEnvironments[0].id === enrollee.studyEnvironmentId)?.study as Study

  return (
    <>
      <h1 className="mb-4">{matchedStudy.name}</h1>
      <StudyTasks enrollee={enrollee} study={matchedStudy} />
    </>
  )
}

const taskTypeDisplayMap: Record<string, string> = {
  CONSENT: 'Consent',
  SURVEY: 'Survey'
}

const enrolleeHasStartedTaskType = (enrollee: Enrollee, taskType: string): boolean => {
  return enrollee.participantTasks
    .filter(task => task.taskType === taskType && (task.status === 'COMPLETE' || task.status === 'IN_PROGRESS'))
    .length > 0
}

type StudyTasksProps = {
  enrollee: Enrollee
  study: Study
}

/** Renders pending tasks for a given study */
function StudyTasks(props: StudyTasksProps) {
  const { enrollee, study } = props

  const hasStudyTasks = enrollee.participantTasks.length > 0

  const sortedActiveConsentTasks = enrollee.participantTasks
    .filter(task => task.taskType === 'CONSENT' && isTaskActive(task))
    .sort(taskComparator)
  const hasActiveConsentTasks = sortedActiveConsentTasks.length > 0

  const sortedSurveyTasks = enrollee.participantTasks
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
    return <div className="fst-italic">No tasks for this study</div>
  }

  return (
    <>
      {nextTask && (
        <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
          <Link
            to={getTaskPath(nextTask, enrollee.shortcode, study.shortcode)}
            className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary"
          >
            {enrolleeHasStartedTaskType(enrollee, nextTask.taskType)
              ? 'Continue'
              : 'Start'}
            {' '}{taskTypeDisplayMap[nextTask.taskType]}
            {numTasksOfNextTaskType > 1 && 's'}
          </Link>
        </div>
      )}

      {hasActiveConsentTasks && (
        <TaskGrouping
          enrollee={enrollee}
          studyShortcode={study.shortcode}
          tasks={sortedActiveConsentTasks}
          title="Consent"
        />
      )}

      {hasSurveyTasks && (
        <TaskGrouping
          enrollee={enrollee}
          tasks={sortedSurveyTasks}
          studyShortcode={study.shortcode}
          title="Surveys"
        />
      )}

      {hasCompletedConsentTasks && (
        <TaskGrouping
          enrollee={enrollee}
          studyShortcode={study.shortcode}
          tasks={completedConsentTasks}
          title="Forms"
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
  return (
    <>
      <h2 className="fs-6 text-uppercase mb-0">{title}</h2>
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
function getNextTask(enrollee: Enrollee, sortedTasks: ParticipantTask[]) {
  const nextTask = sortedTasks.find(task => isTaskAccessible(task, enrollee) && isTaskActive(task))
  return nextTask
}

export const TASK_TYPE_ORDER = ['CONSENT', 'SURVEY']
export const TASK_STATUS_ORDER = ['IN_PROGRESS', 'NEW', 'COMPLETE']

/** Sorts tasks based on their types, then based on status, and then based on their internal ordering */
function taskComparator(taskA: ParticipantTask, taskB: ParticipantTask) {
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
