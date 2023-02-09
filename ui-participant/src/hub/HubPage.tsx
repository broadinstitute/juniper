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
      <h5 className="text-center">Dashboard</h5>
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
  const sortedTasks = enrollee.participantTasks.sort(taskComparator)
  return <div className="p-3">
    <h5 className="mb-3 fw-bold">{matchedStudy.name}</h5>
    {hasStudyTasks && <div>
      <h6 className="fw-bold">Activities</h6>
      <ol style={{ listStyleType: 'none', paddingInlineStart: 0, width: '100%' }}>
        {sortedTasks.map(task => <li key={task.id}>
          <TaskLink task={task} key={task.id} studyShortcode={matchedStudy.shortcode}
            enrollee={enrollee}/>
        </li>)}
      </ol>
    </div>}
    {!hasStudyTasks && <span className="detail">No tasks for this study</span>}

  </div>
}

export const TASK_TYPE_ORDER = ['CONSENT', 'SURVEY']

/** Sorts tasks based on their types, and then based on their internal ordering */
function taskComparator(taskA: ParticipantTask, taskB: ParticipantTask) {
  const typeOrder = TASK_TYPE_ORDER.indexOf(taskA.taskType) - TASK_TYPE_ORDER.indexOf(taskB.taskType)
  if (typeOrder != 0) {
    return typeOrder
  }
  return taskA.taskOrder - taskB.taskOrder
}
