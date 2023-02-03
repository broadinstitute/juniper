import React from 'react'
import { usePortalEnv } from '../providers/PortalProvider'
import { useUser } from '../providers/UserProvider'
import { Enrollee, Portal, Study } from '../api/api'
import TaskLink from './TaskLink'

/** renders the logged-in hub page */
export default function HubPage() {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()

  return <div>
    <div className="container">
      <h5 className="text-center">Hub</h5>
      <div>
        <h5>My Studies</h5>
        {enrollees.map(enrollee => <StudyTaskBox enrollee={enrollee} portal={portal} key={enrollee.id}/>)}
      </div>
    </div>
  </div>
}

/** Renders pending tasks for a given study */
function StudyTaskBox({ enrollee, portal }: { enrollee: Enrollee, portal: Portal }) {
  const matchedStudy = portal.portalStudies
    .find(pStudy => pStudy.study.studyEnvironments[0].id === enrollee.studyEnvironmentId)?.study as Study
  const hasStudyTasks = enrollee.participantTasks.length > 0
  return <div>
    <h6>{matchedStudy.name}</h6>
    {hasStudyTasks && <ol>
      {enrollee.participantTasks.map(task => <TaskLink task={task} key={task.id}
        studyShortcode={matchedStudy.shortcode}/>)}
    </ol>}
    {!hasStudyTasks && <span className="detail">No tasks for this study</span>}

  </div>
}
