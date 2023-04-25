import React from 'react'
import { Link } from 'react-router-dom'
import { Enrollee, ParticipantTask } from 'api/api'
import { faCheck, faCircleHalfStroke, faLock } from '@fortawesome/free-solid-svg-icons'
import { faCircle, faCircleXmark } from '@fortawesome/free-regular-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

export type StatusDisplayInfo = {
  icon: React.ReactNode,
  statusDisplay: string
}

const statusDisplayMap: Record<string, StatusDisplayInfo> = {
  'COMPLETE': {
    icon: <FontAwesomeIcon icon={faCheck} className="fa-lg" style={{ color: 'rgb(122, 152, 188)' }}/>,
    statusDisplay: 'Complete'
  },
  'IN_PROGRESS': {
    icon: <FontAwesomeIcon icon={faCircleHalfStroke} style={{ color: 'rgb(129, 172, 82)' }}/>,
    statusDisplay: 'In Progress'
  },
  'NEW': {
    icon: <FontAwesomeIcon icon={faCircle} style={{ color: '#777' }}/>,
    statusDisplay: 'Not Started'
  },
  'REJECTED': {
    icon: <FontAwesomeIcon icon={faCircleXmark} style={{ color: '#777' }}/>,
    statusDisplay: 'Declined'
  }
}

/**
 *  Renders a link allowing a participant to complete a task
 *  TODO-i18n this uses the task name, which is pulled from the consent/survey name, which is not i18n-able
 *  when we upgrade this UI to support i18n, we'll have to pull task titles by loading parts of the forms themselves,
 *  which do support i18n, and then loading from there.
 * */
export default function TaskLink({ task, studyShortcode, enrollee }:
                                   { task: ParticipantTask, studyShortcode: string, enrollee: Enrollee }) {
  const isAccessible = isTaskAccessible(task, enrollee)
  const styleProps = {
    padding: '1em 0em',
    borderBottom: '1px solid #e4e4e4',
    width: '100%',
    color: isAccessible ? undefined : '#595959'
  }

  return (
    <div className="d-flex flex-row" style={styleProps}>
      <div className="detail">
        {isAccessible
          ? statusDisplayMap[task.status].icon
          : <FontAwesomeIcon icon={faLock} style={{ color: 'rgb(203, 203, 203)' }}/>}
      </div>
      <div className="flex-grow-1 ms-3">
        {isAccessible
          ? <Link to={getTaskPath(task, enrollee.shortcode, studyShortcode)}>{task.targetName}</Link>
          : task.targetName}
      </div>
      <div className="ms-3">
        {isAccessible
          ? statusDisplayMap[task.status].statusDisplay
          : 'Locked'}
      </div>
    </div>
  )
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

/** is the task actionable by the user? the rules are:
 * consent forms are always actionable.
 * nothing else is actionable until consent
 * non-required tasks are not actionable until all required tasks are cleared */
export function isTaskAccessible(task: ParticipantTask, enrollee: Enrollee) {
  const hasRequiredTasks = enrollee.participantTasks.some(task => task.blocksHub && task.status !== 'COMPLETE')
  return task.taskType === 'CONSENT' || (
    enrollee.consented &&
    (!hasRequiredTasks || task.blocksHub)
  )
}

/** is the task ready to be worked on (not done or rejected) */
export function isTaskActive(task: ParticipantTask) {
  return ['NEW', 'IN_PROGRESS'].includes(task.status)
}
