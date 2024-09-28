import { KitRequest } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBoxesPacking, faCircleCheck, faTruckFast } from '@fortawesome/free-solid-svg-icons'
import { mockAssignedKitRequest, mockKitRequest } from '../../test-utils/test-participant-factory'
import React from 'react'
import { Link } from 'react-router-dom'
import { instantToDateString } from '../../util/timeUtils'

export default function KitsPage() {
  return <div
    className="hub-dashboard-background flex-grow-1 mb-2"
    style={{ background: 'var(--dashboard-background-color)' }}>
    <div className="row mx-0 justify-content-center">
      <div className="my-md-4 mx-auto px-0" style={{ maxWidth: 768 }}>
        <div className="card-body">
          <div className="align-items-center">
            <ParticipantKits/>
          </div>
        </div>
      </div>
    </div>
  </div>
}

const getKitStatusBar = (kit: KitRequest) => {
  if (kit.distributionMethod === 'MAILED') {
    return <MailedKitStatusBar kit={kit}/>
  } else if (kit.distributionMethod === 'IN_PERSON') {
    return <InPersonKitStatusBar kit={kit}/>
  } else {
    return <div className="fst-italic text-muted">Status unavailable</div>
  }
}


const MailedKitStatusBar = ({ kit }: { kit: KitRequest }) => {
  console.log(kit)
  return (
    <div className="progress-stacked border-top border-bottom border-start" style={{ height: '50px' }}>
      <ProgressBar width="33%" complete={true}>
        <FontAwesomeIcon icon={faBoxesPacking} className={'fa-xl'} /> Preparing
      </ProgressBar>
      <ProgressBar width="34%" complete={true}>
        <FontAwesomeIcon icon={faTruckFast} className={'fa-xl'} /> Shipped
      </ProgressBar>
      <ProgressBar width="33%" complete={false}>
        <FontAwesomeIcon icon={faCircleCheck} className={'fa-xl'} /> Returned
      </ProgressBar>
    </div>
  )
}

const ProgressBar = ({ children, width, complete }: {
  children: React.ReactNode, width: string, complete: boolean
}) => {
  return (
    <div className="progress" role="progressbar" style={{ width, height: '50px' }}>
      <div className={`progress-bar border-end ${complete ? '' : 'bg-dark-subtle'}`}
        style={{ background: 'var(--brand-color)' }}>
        {children}
      </div>
    </div>
  )
}

const InPersonKitStatusBar = ({ kit }: { kit: KitRequest }) => {
  return <div className="progress-stacked" style={{ height: '50px' }}>
    <div className="progress" role="progressbar" style={{ width: '50%', height: '50px' }}>
      <div className="progress-bar border-end" style={{ background: 'var(--brand-color)' }}>
        <FontAwesomeIcon icon={faBoxesPacking} className={'fa-xl'}/> Created
      </div>
    </div>
    <div className="progress" role="progressbar" style={{ width: '50%', height: '50px' }}>
      <div className="progress-bar border-end" style={{ background: 'var(--brand-color)' }}>
        <FontAwesomeIcon icon={faCircleCheck} className={'fa-xl'}/> Collected
      </div>
    </div>
  </div>
}

const ParticipantKits = () => {
  const mockKit1 = mockKitRequest('CREATED', 'SALIVA')
  const mockKit2 = mockKitRequest('CREATED', 'BLOOD')
  const mockInPersonKit1 = mockAssignedKitRequest('CREATED', 'SALIVA')
  const participantKitRequests = [mockKit1, mockKit2, mockInPersonKit1]

  const isInPersonKitEnabled = true //TODO make this a studyenv setting

  return <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm px-2">
    <h1 className="pb-3">Sample collection kits</h1>
    <div className="pb-4">
      Sample collection kits are a valuable part of the study process and can help provide researchers with
      important insights. Below you will find the status of all kits that have been provided to you.
    </div>
    { isInPersonKitEnabled &&
      <>
        <h3>Provide a sample in-person</h3>
        <div className="pb-2">
          This study is currently offering the option to complete a sample collection kit in-person.
          Click below for more information about this process.
        </div>
        <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
          <Link to={'/hub/kits/in-person'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
            Complete a kit in-person
          </Link>
        </div>
      </>}
    <h3>Your kits ({participantKitRequests.length})</h3>
    <div className="d-flex flex-column">
      {participantKitRequests.length === 0 ? (
        <div className="text-center text-muted fst-italic my-4">
          You have no sample collection kits at this time.
        </div>
      ) : (
        participantKitRequests.map((kit, index) => (
          <div key={index}
            className="d-flex align-items-center justify-content-between mb-3 border rounded-3 p-3">
            <div>
              <div className="fw-bold">{kit.kitType.displayName} Kit</div>
              <div className="fst-italic text-muted">
                {instantToDateString(kit.createdAt / 1000000)}
              </div>
            </div>
            <div style={{ width: '60%' }}>
              {getKitStatusBar(kit)}
            </div>
          </div>
        ))
      )}
    </div>
  </div>
}
