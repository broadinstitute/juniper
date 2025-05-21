import React from 'react'
import { Link } from 'react-router-dom'
import {
  ParticipantTask,
  ParticipantTaskStatus
} from 'api/api'
import {
  faCaretDown,
  faCaretUp,
  faCheck,
  faCircleHalfStroke,
  faLock,
  faMinus,
  faPrint
} from '@fortawesome/free-solid-svg-icons'
import {
  faCircle,
  faCircleXmark
} from '@fortawesome/free-regular-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { hideVisually } from 'polished'
import {
  Enrollee,
  getTaskPath,
  instantToDateString,
  isTaskAccessible,
  useI18n
} from '@juniper/ui-core'

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
  },
  'REMOVED': { // this status isn't displayed anywhere in the UI right now, but is included for completness
    icon: <FontAwesomeIcon icon={faMinus} style={{ color: '#777' }}/>,
    statusDisplayKey: 'taskRemoved'
  }
}

/**
 *  Renders a link allowing a participant to complete a task
 *  when we upgrade this UI to support i18n, we'll have to pull task titles by loading parts of the forms themselves,
 *  which do support i18n, and then loading from there.
 * */
export default function TaskLink({ task, studyShortcode, enrollee, history, nested = false }:
                                   { task: ParticipantTask, studyShortcode: string, enrollee: Enrollee,
                                     history?: ParticipantTask[], nested?: boolean }) {
  const [isExpanded, setIsExpanded] = React.useState(false)
  const isAccessible = isTaskAccessible(task, enrollee)
  const styleProps = {
    color: isAccessible ? undefined : '#595959',
    padding: nested ? '0em' : '1em 0em',
    borderBottom: nested ? undefined : '1px solid #e4e4e4',
    width: '100%'
  }

  const { i18n } = useI18n()

  return (
    <li style={styleProps}>
      <div className="d-flex flex-row flex-grow-1 align-items-center" >
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
          { (task.status === 'NEW' && !!history?.length) && <sup className="ms-1">{i18n('taskNew')}</sup>}
          {(isExpanded || nested) && <span className="text-muted fst-italic ms-2">
            ({instantToDateString(task.createdAt)})
          </span> }
        </div>
        { history && history.length > 0 && <button onClick={() => setIsExpanded(!isExpanded)}
          className="btn btn-link py-0" title={i18n('taskHistory')}>
          <FontAwesomeIcon icon={isExpanded ? faCaretUp : faCaretDown}/>
        </button>}
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
      { isExpanded && history && <ol className="list-unstyled mt-3 w-100">
        {history.map(task =>
          <li className="ps-3" key={task.id}>
            <TaskLink task={task} key={task.id} studyShortcode={studyShortcode} enrollee={enrollee} nested={true}/>
          </li>
        )}
      </ol>}
    </li>
  )
}
