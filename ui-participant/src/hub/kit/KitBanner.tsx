import React from 'react'
import { KitRequest } from '../../api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTruckFast } from '@fortawesome/free-solid-svg-icons'
import { instantToDateString } from '../../util/timeUtils'

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

const iconClass = 'col-xs-2 col-sm-1 col-md-1'
const eventTextClass = 'col-xs-7 col-sm-8 col-md-8'
const eventDateClass = 'col-xs-3 col-sm-3 col-md-3 text-end'
const kitEventClass = 'row mb-3 pt-3'

const renderHeader = () => {
  return <div className={kitEventClass} style={kitHeaderProps}>
    <div className='col-2'>
      STATUS
    </div>
    <div className='col-8'>
    </div>
    <div className='col-2 text-end'>
      DATE
    </div>
  </div>
}

/** Renders kit tasks for the hub page */
export default function KitBanner({ kitRequests }: {kitRequests: KitRequest[]}) {
  const hasKitRequests = kitRequests.length > 0
  if (!hasKitRequests) {
    return null
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
  const unreturnedKits = kitRequests.filter(kitRequest => kitRequest.status === 'SENT')
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
