import React from 'react'
import { KitRequest } from '../api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTruckFast, faVial } from '@fortawesome/free-solid-svg-icons'
import { faRectangleXmark, faSquareCheck } from '@fortawesome/free-regular-svg-icons'


/**
 *
 */
export default function KitSummary({ kitRequests }: {kitRequests: KitRequest[]}) {
  const kitEventProps = {
    padding: '1em 0em',
    borderBottom: '1px solid #e4e4e4',
    width: '100%',
    color: '#595959'
  }

  const kitDividerProps = {
    padding: '1em 0em',
    borderBottom: '2px solid black',
    width: '100%',
    color: 'black'
  }

  const iconProps = {
    color: 'var(--brand-color)',
    fontSize: '30px'
  }

  const iconClass = 'col-1'
  const eventTextClass = 'col-8' // flex-grow-1 ms-4
  const eventDateClass = 'col-3 text-end' // ms-3 flex-lg-column
  const kitEventClass = 'row mb-3 pt-3'

  const hasKitRequests = kitRequests.length > 0
  if (!hasKitRequests) {
    return <div className="fst-italic">No kits</div>
  }

  const hasExceptionStatus = (kitRequest: KitRequest) => {
    return ['ERRORED', 'DEACTIVATED', 'UNKNOWN'].includes(kitRequest.status)
  }

  const hasReceivedStatus = (kitRequest: KitRequest) => {
    return kitRequest.receivedAt != null
  }

  const hasSentStatus = (kitRequest: KitRequest) => {
    return kitRequest.sentAt != null
  }


  const renderExceptionStatus = (kitRequest: KitRequest) => {
    if (!hasExceptionStatus(kitRequest)) {
      return null
    }
    return <div className={kitEventClass} style={kitEventProps}>
      <div className={iconClass}>
        <FontAwesomeIcon className="h2" icon={faRectangleXmark} style={iconProps}/>
      </div>
      <div className={eventTextClass}>
        <div className="fw-bold">There was an issue with your kit.</div>
        <span className="text-muted">The study administrator is investigating.</span>
      </div>
    </div>
  }

  const renderReceivedStatus = (kitRequest: KitRequest) => {
    if (!hasReceivedStatus(kitRequest)) {
      return null
    }
    return <div className={kitEventClass} style={kitEventProps}>
      <div className={iconClass}>
        <FontAwesomeIcon className="h2" icon={faVial} style={iconProps}/>
      </div>
      <div className={eventTextClass}>
        <div className="fw-bold">We received the kit you shipped</div>
        <span className="text-muted">Some additional text here.</span>
      </div>
      <div className={eventDateClass}>
        {instantToDateString(kitRequest.receivedAt)}
      </div>
    </div>
  }

  const renderReturnStatus = (kitRequest: KitRequest) => {
    if (!hasSentStatus(kitRequest) || hasReceivedStatus(kitRequest)) {
      return null
    }
    return <div className={kitEventClass} style={kitEventProps}>
      <div className={iconClass}>
        <FontAwesomeIcon className="h2" icon={faVial} style={iconProps}/>
      </div>
      <div className={eventTextClass}>
        <div className="fw-bold">Complete and return your kit</div>
        <span className="text-muted">Once your receive your kit, please complete and send it back to us </span>
        <span className="text-muted">with the prepaid envelope provided.</span>
      </div>
      <div className={eventDateClass}/>
    </div>
  }

  const renderSentStatus = (kitRequest: KitRequest) => {
    if (!hasSentStatus(kitRequest)) {
      return null
    }
    return <div className={kitEventClass} style={kitEventProps}>
      <div className={iconClass}>
        <FontAwesomeIcon className="h2" icon={faTruckFast} style={iconProps}/>
      </div>
      <div className={eventTextClass}>
        <div className="fw-bold">A sample kit was shipped</div>
        <span className="text-muted">Your {kitRequest.kitType.displayName.toLowerCase()} kit is on its way. </span>
        <span className="text-muted">Look for a small box in the mail.</span>
      </div>
      <div className={eventDateClass}>
        {instantToDateString(kitRequest.sentAt)}
      </div>
    </div>
  }

  const renderRequestStatus = (kitRequest: KitRequest) => {
    return <div className={kitEventClass} style={kitEventProps}>
      <div className={iconClass}>
        <FontAwesomeIcon className="h2" icon={faSquareCheck} style={iconProps}/>
      </div>
      <div className={eventTextClass}>
        <div className="fw-bold">A sample kit was requested for you</div>
      </div>
      <div className={eventDateClass}>
        {instantToDateString(kitRequest.createdAt)}
      </div>
    </div>
  }

  const renderHeader = () => {
    return <div className={kitEventClass} style={kitEventProps}>
      <div className={iconClass}>
        STATUS
      </div>
      <div className={eventTextClass}>
      </div>
      <div className={eventDateClass}>
        DATE
      </div>
    </div>
  }

  // <div className="list-unstyled p-0" style={kitDividerProps}> -->

  let kitNum = 0
  return (
    <div>
      {kitRequests.map(kitRequest => {
        kitNum = kitNum + 1
        return <>
          <h3 className="h4 mb-3">{kitRequest.kitType.displayName} kit</h3>
            <div className='container'>
            {renderHeader()}
            {renderExceptionStatus(kitRequest)}
            {renderReceivedStatus(kitRequest)}
            {renderReturnStatus(kitRequest)}
            {renderSentStatus(kitRequest)}
            {renderRequestStatus(kitRequest)}
              {kitRequests.length > kitNum && <div className="list-unstyled pt-4"/>}
          </div>
        </>
      })}
    </div>
  )
}

// TODO: this is identical to a method in ui-admin/src/util/timeUtils.tsx
// move that to ui-core (I had some trouble with that so put off the change) -DC
function instantToDateString(instant?: number) {
  if (!instant) {
    return ''
  }
  return new Date(instant * 1000).toLocaleDateString()
}
