import React from 'react'
import { usePortalEnv } from '../providers/PortalProvider'
import { useUser } from '../providers/UserProvider'

import { Enrollee, ParticipantTask, Portal, PortalStudy, Study } from '../api/api'
import TaskLink, { getTaskPath, isTaskAccessible, isTaskActive } from './TaskLink'
import { Link, NavLink, useLocation } from 'react-router-dom'

import TaskStatusMessage from './TaskStatusMessage'

export type HubUpdate = {
  message: {
    content: string,
    messageType: string
  }
}

/** renders the logged-in hub page */
export default function HubPage() {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()
  const location = useLocation()
  /**
   * Pull any messages to be displayed as a result of where we came from e.g. "survey complete"
   * This is in accord with recommended usage of location state with React Router v6.
   */
  const hubUpdate: HubUpdate | undefined = location.state
  const hubMessage = hubUpdate?.message
  const unjoinedStudies = portal.portalStudies.filter(pStudy => !userHasJoinedPortalStudy(pStudy, enrollees))
  const hasUnjoinedStudies = unjoinedStudies.length > 0
  return (
    <div
      className="flex-grow-1"
      style={{ background: 'linear-gradient(270deg, #D5ADCC 0%, #E5D7C3 100%' }}
    >
      <div
        className="hub-dashboard py-4 px-2 px-md-5 my-md-5 mx-auto shadow-sm"
        style={{ background: '#fff', maxWidth: 768 }}
      >
        <h1>{portal.name}</h1>

        {!!hubMessage && (
          <div className="mb-2">
            <TaskStatusMessage content={hubMessage.content} messageType={hubMessage.messageType}/>
          </div>
        )}

        {enrollees.map(enrollee => <StudyTaskBox enrollee={enrollee} portal={portal} key={enrollee.id}/>)}

        {hasUnjoinedStudies && (
          <>
            <h2 className="text-center">Studies you can join</h2>
            <ul className="list-group">
              {unjoinedStudies.map(portalStudy => <li key={portalStudy.study.shortcode} className="list-group-item">
                <h6>{portalStudy.study.name}</h6>
                <NavLink to={`/studies/${portalStudy.study.shortcode}/join`}>Join</NavLink>
              </li>)}
            </ul>
          </>
        )}
      </div>
    </div>
  )
}

const taskTypeDisplayMap: Record<string, string> = {
  CONSENT: 'Consent',
  SURVEY: 'Survey'
}

/** Renders pending tasks for a given study */
function StudyTaskBox({ enrollee, portal }: { enrollee: Enrollee, portal: Portal }) {
  const matchedStudy = portal.portalStudies
    .find(pStudy => pStudy.study.studyEnvironments[0].id === enrollee.studyEnvironmentId)?.study as Study
  const hasStudyTasks = enrollee.participantTasks.length > 0
  const sortedConsentTasks = enrollee.participantTasks.filter(task => task.taskType === 'CONSENT' &&
    isTaskActive(task)).sort(taskComparator)
  const hasActiveConsentTasks = sortedConsentTasks.length > 0
  const sortedSurveyTasks = enrollee.participantTasks.filter(task => task.taskType === 'SURVEY').sort(taskComparator)
  const hasActiveSurveyTasks = sortedSurveyTasks.length > 0
  const nextTask = getNextTask(enrollee, [...sortedConsentTasks, ...sortedSurveyTasks])
  const completedForms = enrollee.participantTasks.filter(task => task.status === 'COMPLETE' &&
    task.taskType === 'CONSENT')
  const hasCompletedForms = completedForms.length > 0

  return <div className="p-3">
    {hasStudyTasks && <div>
      {nextTask && <div className="row">
        <div className="col-md-12 text-center p-4" style={{ background: '#eef' }}>
          <Link to={getTaskPath(nextTask, enrollee.shortcode, matchedStudy.shortcode)}
            className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
            Continue {taskTypeDisplayMap[nextTask.taskType]}s
          </Link>
        </div>
      </div>}
      {hasActiveConsentTasks && <TaskGrouping title="CONSENT" tasks={sortedConsentTasks} enrollee={enrollee}
        studyShortcode={matchedStudy.shortcode}/>}
      {hasActiveSurveyTasks && <TaskGrouping title="SURVEYS" tasks={sortedSurveyTasks} enrollee={enrollee}
        studyShortcode={matchedStudy.shortcode}/>}
      {hasCompletedForms && <TaskGrouping title="FORMS" tasks={completedForms} enrollee={enrollee}
        studyShortcode={matchedStudy.shortcode}/>}
    </div>}
    {!hasStudyTasks && <span className="detail">No tasks for this study</span>}
  </div>
}

/** renders a group like "CONSENTS" or "SURVEYS" */
function TaskGrouping({ title, tasks, enrollee, studyShortcode }: {
  title: string, tasks: ParticipantTask[],
  enrollee: Enrollee, studyShortcode: string
}) {
  return <div className="mt-4">
    <span className="fw-bold">{title}</span>
    <ol style={{ listStyleType: 'none', paddingInlineStart: 0, width: '100%' }}>
      {tasks.map(task => <li key={task.id}>
        <TaskLink task={task} key={task.id} studyShortcode={studyShortcode}
          enrollee={enrollee}/>
      </li>)}
    </ol>
  </div>
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

/** whether the list of enrollees contains an enrollee matching the study */
function userHasJoinedPortalStudy(portalStudy: PortalStudy, enrollees: Enrollee[]) {
  return !!enrollees.find(enrollee => enrollee.studyEnvironmentId === portalStudy.study.studyEnvironments[0].id)
}
