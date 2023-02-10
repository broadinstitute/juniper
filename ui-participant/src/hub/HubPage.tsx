import React from 'react'
import { usePortalEnv } from '../providers/PortalProvider'
import { useUser } from '../providers/UserProvider'
import { Enrollee, ParticipantTask, Portal, Study } from '../api/api'
import TaskLink from './TaskLink'
import { useLocation } from 'react-router-dom'
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

  return <div>
    <div className="container">
      <h5 className="text-center">Hub</h5>
      {!!hubMessage && <div className="row mb-2">
        <div className="col-md-12">
          <TaskStatusMessage content={hubMessage.content} messageType={hubMessage.messageType}/>
        </div>
      </div>}
      <div className="row">
        <div className="col-md-6">
          {enrollees.map(enrollee => <StudyTaskBox enrollee={enrollee} portal={portal} key={enrollee.id}/>)}
        </div>
      </div>
    </div>
  </div>
}


/** Renders pending tasks for a given study */
function StudyTaskBox({ enrollee, portal }: { enrollee: Enrollee, portal: Portal }) {
  const matchedStudy = portal.portalStudies
    .find(pStudy => pStudy.study.studyEnvironments[0].id === enrollee.studyEnvironmentId)?.study as Study
  const hasStudyTasks = enrollee.participantTasks.length > 0
  const activeTasks = enrollee.participantTasks.filter(task => PENDING_STATUSES.includes(task.status))
    .sort(taskComparator)
  const inactiveTasks = enrollee.participantTasks.filter(task => !PENDING_STATUSES.includes(task.status))
    .sort(taskComparator)
  return <div className="p-3">
    <h5 className="mb-3 fw-bold">{matchedStudy.name}</h5>
    {hasStudyTasks && <div>
      <h6 className="fw-bold">Activities</h6>
      <ol style={{ listStyleType: 'none', paddingInlineStart: 0, width: '100%' }}>
        {activeTasks.map(task => <li key={task.id}>
          <TaskLink task={task} key={task.id} studyShortcode={matchedStudy.shortcode}
            enrolleeShortcode={enrollee.shortcode}/>
        </li>)}
      </ol>
      <ol style={{ listStyleType: 'none', paddingInlineStart: 0, width: '100%' }}>
        {inactiveTasks.map(task => <li key={task.id}>
          <TaskLink task={task} key={task.id} studyShortcode={matchedStudy.shortcode}
            enrolleeShortcode={enrollee.shortcode}/>
        </li>)}
      </ol>
    </div>}
    {!hasStudyTasks && <span className="detail">No tasks for this study</span>}

  </div>
}

export const PENDING_STATUSES = ['NEW', 'IN_PROGRESS']

/** Sorts tasks based on their internal ordering */
function taskComparator(taskA: ParticipantTask, taskB: ParticipantTask) {
  return taskA.taskOrder - taskB.taskOrder
}
