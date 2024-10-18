import React from 'react'
import { Link } from 'react-router-dom'
import { ParticipantTask, ParticipantTaskStatus } from 'api/api'
import { faCheck, faCircleHalfStroke, faLock, faPrint } from '@fortawesome/free-solid-svg-icons'
import { faCircle, faCircleXmark } from '@fortawesome/free-regular-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { hideVisually } from 'polished'
import { Enrollee, useI18n } from '@juniper/ui-core'
import { getTaskPath, isTaskAccessible } from './task/taskUtils'

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
            {i18n(`${task.targetStableId}:${task.targetAssignedVersion}`, { defaultValue: task.targetName })}
          </Link>
          : i18n(`${task.targetStableId}:${task.targetAssignedVersion}`, { defaultValue: task.targetName })}
      </div>
      {task.taskType === 'CONSENT' && task.status === 'COMPLETE' && (
        <div className="ms-3">
          <Link to={`${getTaskPath(task, enrollee.shortcode, studyShortcode, true)}`}>
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
