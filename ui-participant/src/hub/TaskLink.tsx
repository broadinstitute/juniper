import React from 'react'
import {Link} from 'react-router-dom'
import {Enrollee, ParticipantTask} from 'api/api'
import {faCheck, faChevronRight, faCircleHalfStroke, faLock, faTimesCircle} from '@fortawesome/free-solid-svg-icons'
import {faCircle} from '@fortawesome/free-regular-svg-icons'
import {FontAwesomeIcon} from '@fortawesome/react-fontawesome'

export type StatusDisplayInfo = {
  icon: React.ReactNode,
  statusDisplay: string,
  actionDisplay?: React.ReactNode
}

const statusDisplayMap: Record<string, StatusDisplayInfo> = {
  'COMPLETE': {
    icon: <FontAwesomeIcon className="fa-lg" style={{color: 'green'}} icon={faCheck}/>,
    statusDisplay: 'Completed', actionDisplay: 'View'
  },
  'IN_PROGRESS': {
    icon: <FontAwesomeIcon icon={faCircleHalfStroke}/>,
    statusDisplay: 'In progress',
    actionDisplay: <span>Start <FontAwesomeIcon icon={faChevronRight}/></span>
  },
  'NEW': {
    icon: <FontAwesomeIcon icon={faCircle}/>,
    statusDisplay: 'Not started',
    actionDisplay: <span>Start <FontAwesomeIcon className="me-1" icon={faChevronRight}/></span>
  },
  'REJECTED': {
    icon: <FontAwesomeIcon icon={faTimesCircle}/>,
    statusDisplay: 'Declined', actionDisplay: 'View'
  }
}

/**
 *  Renders a link allowing a participant to complete a task
 *  TODO-i18n this uses the task name, which is pulled from the consent/survey name, which is not i18n-able
 *  when we upgrade this UI to support i18n, we'll have to pull task titles by loading parts of the forms themselves,
 *  which do support i18n, and then loading from there.
 * */
export default function TaskLink({task, studyShortcode, enrollee}:
                                   { task: ParticipantTask, studyShortcode: string, enrollee: Enrollee }) {
  const isAccessible = isTaskAccessible(task, enrollee)
  const styleProps = {
    padding: '1em 0em',
    borderBottom: '1px solid #e4e4e4',
    width: '100%',
    color: isAccessible ? undefined : '#aaa'
  }
  if (!isAccessible) {
    return <div className="d-flex flex-row" style={styleProps}>
      <div><FontAwesomeIcon icon={faLock} title="you must consent for the study"/></div>
      <div className="flex-grow-1 ms-3">{task.targetName}</div>
    </div>
  }
  return <div className="d-flex flex-row" style={styleProps}>
    <div>
      {statusDisplayMap[task.status].icon}
    </div>
    <div className="flex-grow-1 ms-3">
      <Link to={getTaskPath(task, enrollee.shortcode, studyShortcode)}>{task.targetName}</Link>
    </div>
    <div className="ms-3">
      {statusDisplayMap[task.status].statusDisplay}
    </div>
  </div>
}

/** returns a string for including in a <Link to={}> link to be navigated by the participant */
export function getTaskPath(task: ParticipantTask, enrolleeShortcode: string, studyShortcode: string): string {
  if (task.taskType === 'CONSENT') {
    return `study/${studyShortcode}/enrollee/${enrolleeShortcode}/consent/${task.targetStableId}`
      + `/${task.targetAssignedVersion}?taskId=${task.id}`
  } else if (task.taskType === 'SURVEY') {
    return `/hub/study/${studyShortcode}/enrollee/${enrolleeShortcode}/survey/${task.targetStableId}`
      + `/${task.targetAssignedVersion}?taskId=${task.id}`
  }
  return ''
}

/** is the task actionable by the user? for now, just looks at whether they've consented */
export function isTaskAccessible(task: ParticipantTask, enrollee: Enrollee) {
  return task.taskType === 'CONSENT' || enrollee.consented
}

/** is the task ready to be worked on (not done or rejected) */
export function isTaskActive(task: ParticipantTask) {
  return ['NEW', 'IN_PROGRESS'].includes(task.status)
}

