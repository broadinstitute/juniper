import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleCheck, faHandHolding, faQuestion, faTruckFast } from '@fortawesome/free-solid-svg-icons'
import { KitRequest, useI18n } from '@juniper/ui-core'
import { NavLink } from 'react-router-dom'
import { instantToDateString } from 'util/timeUtils'

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
const kitEventClass = 'row pt-3'

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
  const { i18n } = useI18n()
  const hasKitRequests = kitRequests.length > 0
  if (!hasKitRequests) {
    return null
  }

  // for now, only show un-returned kits or kits distributed in person
  const kitsToDisplay = kitRequests
    .filter(kitRequest =>
      kitRequest.status === 'SENT' || kitRequest.distributionMethod === 'IN_PERSON')

  if (kitsToDisplay.length === 0) {
    return null
  }

  return (
    <>
      <h2 className="fs-6 text-uppercase mb-0">{i18n('kitsPageTitle')}</h2>
      {renderHeader()}
      <ul className="list-unstyled">
        {kitsToDisplay.map(kitRequest => {
          return <li className="container" key={kitRequest.id}>
            <KitStatus kitRequest={kitRequest}/>
          </li>
        })}
      </ul>
    </>
  )
}

const titleForStatus = (status: string) => {
  switch (status) {
    case 'SENT':
      return 'A sample kit was shipped'
    case 'CREATED':
      return 'You have received a sample kit'
    case 'COLLECTED_BY_STAFF':
      return 'Your sample kit has been received'
  }
}

const iconForStatus = (status: string) => {
  switch (status) {
    case 'SENT':
      return faTruckFast
    case 'CREATED':
      return faHandHolding
    case 'COLLECTED_BY_STAFF':
      return faCircleCheck
    default:
      return faQuestion
  }
}

const descriptionForStatus = (kitRequest: KitRequest) => {
  switch (kitRequest.status) {
    case 'SENT':
      return <>
        Your {kitRequest.kitType.displayName.toLowerCase()} kit is on its way.
        Once you receive your kit, please complete and send it back to us
        with the prepaid envelope provided.
      </>
    case 'CREATED':
      return <>
        A member of the study team has provided you
        with a {kitRequest.kitType.displayName.toLowerCase()} kit in-person.
        Please complete the kit and return it to the study team. <NavLink to={'/hub/kits'}>
        Click here</NavLink> for more information.
      </>
    case 'COLLECTED_BY_STAFF':
      return <>
        A member of the study team has collected your
        completed {kitRequest.kitType.displayName.toLowerCase()} kit.
        Thank you for your participation.
      </>
  }
}

function KitStatus({ kitRequest }: {kitRequest: KitRequest}) {
  return <div className={kitEventClass} style={kitEventProps}>
    <div className="d-flex justify-content-between px-0">
      <div className={iconClass}>
        <FontAwesomeIcon className="pt-1 pe-2" icon={iconForStatus(kitRequest.status)} style={iconProps}/>
      </div>
      <div className={eventTextClass}>
        <div className="fw-bold">{titleForStatus(kitRequest.status)}</div>
        <span className="text-muted">
          {descriptionForStatus(kitRequest)}
        </span>
      </div>
      <div className={eventDateClass}>
        {kitRequest.distributionMethod === 'IN_PERSON' ?
          instantToDateString(kitRequest.createdAt) : instantToDateString(kitRequest.sentAt)}
      </div>
    </div>
  </div>
}
