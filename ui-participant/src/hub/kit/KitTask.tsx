import React from 'react'
import { KitRequest } from '../../api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTruckFast } from '@fortawesome/free-solid-svg-icons'

/** Renders kit tasks for the hub page */
export default function KitTask({ kitRequests }: {kitRequests: KitRequest[]}) {
  const kitEventProps = {
    padding: '1em 0em',
    borderBottom: '1px solid #e4e4e4',
    width: '100%',
    color: '#595959'
  }

  const kitHeaderProps = {
    borderBottom: '1px solid #e4e4e4',
    width: '100%',
    color: '#595959'
  }

  const iconProps = {
    color: 'var(--brand-color)',
    fontSize: '30px'
  }

  const iconClass = 'col-1'
  const eventTextClass = 'col-8'
  const eventDateClass = 'col-3 text-end'
  const kitEventClass = 'row mb-3 pt-3'

  const hasKitRequests = kitRequests.length > 0
  if (!hasKitRequests) {
    return null
  }

  const unreturnedKit = (kitRequest: KitRequest) => {
    return kitRequest.status === 'SENT'
  }

  const renderHeader = () => {
    return <div className={kitEventClass} style={kitHeaderProps}>
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
    return <div className={kitEventClass} style={kitEventProps}>
      <div className={iconClass}>
        <FontAwesomeIcon className="h2" icon={faTruckFast} style={iconProps}/>
      </div>
      <div className={eventTextClass}>
        <div className="fw-bold">A sample kit was shipped</div>
        <span className="text-muted">Your {kitRequest.kitType.displayName.toLowerCase()} kit is on its way. </span>
        <span className="text-muted">Once your receive your kit, please complete and send it back to us </span>
        <span className="text-muted">with the prepaid envelope provided.</span>
      </div>
      <div className={eventDateClass}>
        {kitRequest.sentAt && instantToDateString(kitRequest.sentAt)}
      </div>
    </div>
  }

  // for now, only show un-returned kits
  const unreturnedKits = kitRequests.filter(kitRequest => unreturnedKit(kitRequest))
  if (unreturnedKits.length === 0) {
    return null
  }

  return (
    <div>
      {unreturnedKits.map(kitRequest => {
        return <div key={kitRequest.id}>
          <h2 className="fs-6 text-uppercase mb-0">Sample collection kits</h2>
          <div className='container'>
            {renderHeader()}
            {renderSentStatus(kitRequest)}
            <div className="list-unstyled pt-4"/>
          </div>
        </div>
      })}
    </div>
  )
}

// TODO: this is identical to a method in ui-admin/src/util/timeUtils.tsx
// move that to ui-core and use it here (there are other methods that should
// also be moved so: JN-781 ) -DC
/** Returns a locale date string given a java Instant. */
export function instantToDateString(instant?: number) {
  if (!instant) {
    return ''
  }
  return new Date(instant * 1000).toLocaleDateString()
}
