import React from 'react'
import {Link} from 'react-router-dom'
import {ParticipantTask} from 'api/api'
import {faCircleCheck, faCircleHalfStroke, faTimesCircle} from "@fortawesome/free-solid-svg-icons"
import {faCircle} from '@fortawesome/free-regular-svg-icons'
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

export const HIGHLIGHT_STATUSES = ['NEW', 'IN_PROGRESS']

const statusIconMap: Record<string, React.ReactNode> = {
  'COMPLETE': <FontAwesomeIcon icon={faCircleCheck}/>,
  'IN_PROGRESS': <FontAwesomeIcon icon={faCircleHalfStroke}/>,
  'NEW': <FontAwesomeIcon icon={faCircle}/>,
  'REJECTED': <FontAwesomeIcon icon={faTimesCircle} style={{color: 'red'}}/>
}

/** Renders a link allowing a participant to complete a task */
export default function TaskLink({task, studyShortcode, enrolleeShortcode}:
                                   { task: ParticipantTask, studyShortcode: string, enrolleeShortcode: string }) {
  if (task.taskType === 'CONSENT') {
    return <ConsentLink task={task} studyShortcode={studyShortcode} enrolleeShortcode={enrolleeShortcode}/>
  } else if (task.taskType === 'SURVEY') {
    return <SurveyLink task={task} studyShortcode={studyShortcode} enrolleeShortcode={enrolleeShortcode}/>
  }
  return <span>unknown task type</span>
}

/** renders a link allowing a participant to complete a consent form */
export function ConsentLink({task, studyShortcode, enrolleeShortcode}:
                              { task: ParticipantTask, studyShortcode: string, enrolleeShortcode: string }) {
  let target = `study/${studyShortcode}/enrollee/${enrolleeShortcode}/consent/${task.targetStableId}`
    + `/${task.targetAssignedVersion}?taskId=${task.id}`
  return <Link to={target} style={taskLinkStyle(task)}>
    <TaskIcon task={task}/> {task.targetName}
  </Link>
}

/** renders a link allowing a participant to complete a consent form  */
export function SurveyLink({task, studyShortcode, enrolleeShortcode}:
                             { task: ParticipantTask, studyShortcode: string, enrolleeShortcode: string }) {
  const target = `/hub/study/${studyShortcode}/enrollee/${enrolleeShortcode}/survey/${task.targetStableId}`
    + `/${task.targetAssignedVersion}?taskId=${task.id}`
  return <Link to={target} style={taskLinkStyle(task)}>
    <TaskIcon task={task}/> {task.targetName}
  </Link>
}


function TaskIcon({task}: { task: ParticipantTask }) {
  return <span className="me-2">{statusIconMap[task.status]}</span>
}

function taskLinkStyle(task: ParticipantTask) {
  const isHighlighted = HIGHLIGHT_STATUSES.includes(task.status)
  return {
    padding: '0.5em 2em',
    borderRadius: '5px',
    background: isHighlighted ? '#fff' : '#ddd',
    color: isHighlighted ? undefined : '#777',
    display: 'block',
    width: '100%'
  }
}


