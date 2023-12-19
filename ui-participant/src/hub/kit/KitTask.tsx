import React from 'react'
import { KitRequest } from '../../api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTruckFast, faVial } from '@fortawesome/free-solid-svg-icons'
import { faRectangleXmark, faSquareCheck } from '@fortawesome/free-regular-svg-icons'


/**
 *
 */
export default function KitTask({ kitRequests }: {kitRequests: KitRequest[]}) {
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

  const hasReceivedStatus = (kitRequest: KitRequest) => {
    return kitRequest.receivedAt != null
  }

  const hasSentStatus = (kitRequest: KitRequest) => {
    return kitRequest.sentAt != null
  }

  const unreturnedKit = (kitRequest: KitRequest) => {
    return hasSentStatus(kitRequest) && !hasReceivedStatus(kitRequest)
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

  const renderSentStatus = (kitRequest: KitRequest) => {
    if (!hasSentStatus(kitRequest) || hasReceivedStatus(kitRequest)) {
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

  // only show un-returned kits
  const unreturnedKits = kitRequests.filter(kitRequest => unreturnedKit(kitRequest))
  if (unreturnedKits.length === 0) {
    return null
  }

  return (
    <div>
      {unreturnedKits.map(kitRequest => {
        return <>
          <h2 className="fs-6 text-uppercase mb-0">Sample collection kits</h2>
          <div className='container'>
            {renderHeader()}
            {renderSentStatus(kitRequest)}
            <div className="list-unstyled pt-4"/>
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
