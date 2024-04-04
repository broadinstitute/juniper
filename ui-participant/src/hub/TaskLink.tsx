import React from 'react'
import { Link } from 'react-router-dom'
import { Enrollee, ParticipantTask, ParticipantTaskStatus } from 'api/api'
import { faCheck, faCircleHalfStroke, faLock, faPrint } from '@fortawesome/free-solid-svg-icons'
import { faCircle, faCircleXmark } from '@fortawesome/free-regular-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { hideVisually } from 'polished'
import { useI18n } from '@juniper/ui-core'

export type StatusDisplayInfo = {
  icon: React.ReactNode,
  statusDisplayKey: string
}

const statusDisplayMap: Record<ParticipantTaskStatus, StatusDisplayInfo> = {
  'COMPLETE': {
    icon: <FontAwesomeIcon icon={faCheck} className="fa-lg" style={{ color: 'rgb(122, 152, 188)' }}/>,
    statusDisplayKey: 'taskComplete'
  },
  'IN_PROGRESS': {
    icon: <FontAwesomeIcon icon={faCircleHalfStroke} style={{ color: 'rgb(129, 172, 82)' }}/>,
    statusDisplayKey: 'taskInProgress'
  },
  'NEW': {
    icon: <FontAwesomeIcon icon={faCircle} style={{ color: '#777' }}/>,
    statusDisplayKey: 'taskNotStarted'
  },
  'REJECTED': {
    icon: <FontAwesomeIcon icon={faCircleXmark} style={{ color: '#777' }}/>,
    statusDisplayKey: 'taskDeclined'
  },
  'VIEWED': {
    icon: <FontAwesomeIcon icon={faCircle} style={{ color: '#777' }}/>,
    statusDisplayKey: 'taskNotStarted'
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

  const { i18n } = useI18n()

  return (
    <div className="d-flex flex-row" style={styleProps}>
      <div className="detail">
        {isAccessible
          ? statusDisplayMap[task.status].icon
          : <FontAwesomeIcon icon={faLock} style={{ color: 'rgb(203, 203, 203)' }}/>}
      </div>
      <div className="flex-grow-1 ms-3">
        {isAccessible
          ? <Link to={getTaskPath(task, enrollee.shortcode, studyShortcode)}>
            {i18n(`${task.targetStableId}:${task.targetAssignedVersion}`, task.targetName)}
          </Link>
          : i18n(`${task.targetStableId}:${task.targetAssignedVersion}`, task.targetName)}
      </div>
      {task.taskType === 'CONSENT' && task.status === 'COMPLETE' && (
        <div className="ms-3">
          <Link to={`${getTaskPath(task, enrollee.shortcode, studyShortcode)}/print`}>
            <FontAwesomeIcon icon={faPrint} />
            <span style={hideVisually()}>Print {task.targetName}</span>
          </Link>
        </div>
      )}
      <div className="ms-3">
        {isAccessible
          ? i18n(statusDisplayMap[task.status].statusDisplayKey)
          : i18n('taskLocked')}
      </div>
    </div>
  )
}

/** returns a string for including in a <Link to={}> link to be navigated by the participant */
export function getTaskPath(task: ParticipantTask, enrolleeShortcode: string, studyShortcode: string): string {
  if (task.taskType === 'CONSENT') {
    return `study/${studyShortcode}/enrollee/${enrolleeShortcode}/consent/${task.targetStableId}`
      + `/${task.targetAssignedVersion}`
  } else if (task.taskType === 'SURVEY') {
    return `/hub/study/${studyShortcode}/enrollee/${enrolleeShortcode}/survey/${task.targetStableId}`
      + `/${task.targetAssignedVersion}?taskId=${task.id}`
  } else if (task.taskType === 'OUTREACH') {
    return `/hub/study/${studyShortcode}/enrollee/${enrolleeShortcode}/outreach/${task.targetStableId}`
        + `/${task.targetAssignedVersion}?taskId=${task.id}`
  }
  return ''
}

/** is the task actionable by the user? the rules are:
 * consent forms are always actionable.
 * nothing else is actionable until consent
 * required tasks must be done in-order
 * non-required tasks are not actionable until all required tasks are cleared */
export function isTaskAccessible(task: ParticipantTask, enrollee: Enrollee) {
  if (task.taskType === 'CONSENT' || task.status === 'COMPLETE') {
    return true
  }
  const openConsents = enrollee.participantTasks
    .filter(task => task.taskType === 'CONSENT' && task.status !== 'COMPLETE')
  if (openConsents.length) {
    return false
  }
  const openRequiredTasks = enrollee.participantTasks.filter(task => task.blocksHub && task.status !== 'COMPLETE')
    .sort((a, b) => a.taskOrder - b.taskOrder)
  if (openRequiredTasks.length) {
    return task.id === openRequiredTasks[0].id
  }
  return true
}

/** is the task ready to be worked on (not done or rejected) */
export function isTaskActive(task: ParticipantTask) {
  return ['NEW', 'VIEWED', 'IN_PROGRESS'].includes(task.status)
}
