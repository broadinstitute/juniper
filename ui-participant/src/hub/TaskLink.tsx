import React from 'react'
import { Link } from 'react-router-dom'
import { ParticipantTask } from '../api/api'

/** Renders a link allowing a participant to complete a task */
export default function TaskLink({ task, studyShortcode }: { task: ParticipantTask, studyShortcode: string }) {
  if (task.taskType === 'CONSENT') {
    return <ConsentLink task={task} studyShortcode={studyShortcode}/>
  }
  return <span>unknown task type</span>
}

/** renders a link allowing a participant to complete a consent form */
export function ConsentLink({ task, studyShortcode }: { task: ParticipantTask, studyShortcode: string }) {
  return <Link to={`/hub/study/${studyShortcode}/consent/${task.targetStableId}/${task.targetAssignedVersion}`}>
    {task.targetName}
  </Link>
}

