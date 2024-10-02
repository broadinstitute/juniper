import { Enrollee, KitRequest, KitRequestStatus } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBoxesPacking, faCircleCheck, faTruckFast } from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import { Link } from 'react-router-dom'
import { instantToDateString } from 'util/timeUtils'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { usePortalEnv } from '../../providers/PortalProvider'

export default function KitsPage() {
  const { enrollees, ppUser } = useActiveUser()

  return <div
    className="hub-dashboard-background flex-grow-1 pb-2"
    style={{ background: 'var(--dashboard-background-color)' }}>
    <div className="row mx-0 justify-content-center">
      <div className="my-md-4 mx-auto px-0" style={{ maxWidth: 768 }}>
        <div className="card-body">
          <div className="align-items-center">
            <EnrolleeKitRequests enrollee={enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)!}/>
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

const getStepFromStatus = (status: KitRequestStatus): string => {
  switch (status) {
    case 'NEW':
    case 'CREATED':
    case 'QUEUED':
      return 'PREPARING'
    case 'SENT':
      return 'SHIPPED'
    case 'COLLECTED_BY_STAFF':
      return 'COLLECTED'
    case 'RECEIVED':
      return 'RETURNED'
    default:
      return 'UNKNOWN'
  }
}

const getStepCompletion = (kit: KitRequest, step: string) => {
  const statusOrder = ['PREPARING', 'SHIPPED', 'COLLECTED', 'RETURNED']
  const currentStep = getStepFromStatus(kit.status)
  const currentStatusIndex = statusOrder.indexOf(currentStep)
  const stepIndex = statusOrder.indexOf(step)
  return stepIndex <= currentStatusIndex
}

const MailedKitStatusBar = ({ kit }: { kit: KitRequest }) => {
  return (
    <div className="progress-stacked border-top border-bottom border-start" style={{ height: '50px' }}>
      <ProgressBar
        width="33%" complete={getStepCompletion(kit, 'PREPARING')} icon={faBoxesPacking} label={'Preparing'}/>
      <ProgressBar
        width="34%" complete={getStepCompletion(kit, 'SHIPPED')} icon={faTruckFast} label={'Shipped'}/>
      <ProgressBar
        width="33%" complete={getStepCompletion(kit, 'RETURNED')} icon={faCircleCheck} label={'Returned'}/>
    </div>
  )
}

const InPersonKitStatusBar = ({ kit }: { kit: KitRequest }) => {
  return (
    <div className="progress-stacked" style={{ height: '50px' }}>
      <ProgressBar
        width="50%" complete={getStepCompletion(kit, 'PREPARING')} icon={faBoxesPacking} label={'Created'}/>
      <ProgressBar
        width="50%" complete={getStepCompletion(kit, 'COLLECTED')} icon={faCircleCheck} label={'Collected'}/>
    </div>
  )
}

const ProgressBar = ({ icon, label, width, complete }: {
  icon: IconDefinition, label: string, width: string, complete: boolean
}) => {
  return (
    <div className="progress" role="progressbar" style={{ width, height: '50px' }}>
      <div className={`progress-bar border-end ${complete ? '' : 'bg-dark-subtle'}`}
        data-testid={`${label.toLowerCase()}-${complete}`}
        style={{ background: 'var(--brand-color)' }}>
        <div className="d-flex flex-column align-items-center justify-content-center h-100">
          <FontAwesomeIcon icon={icon} className={'fa-xl'}/>
          {label}
        </div>
      </div>
    </div>
  )
}

const EnrolleeKitRequests = ({ enrollee }: { enrollee: Enrollee }) => {
  const enrolleeKitRequests = enrollee.kitRequests
  const visibleKitRequests = enrolleeKitRequests.filter(kit =>
    kit.status !== 'DEACTIVATED' && kit.status !== 'ERRORED').sort((a, b) => b.createdAt - a.createdAt)

  const { portal, portalEnv } = usePortalEnv()
  const currentStudyEnv = portal.portalStudies.find(pStudy =>
    pStudy.study.studyEnvironments.find(studyEnv =>
      studyEnv.environmentName === portalEnv.environmentName))?.study.studyEnvironments[0]
  const isInPersonKitEnabled = currentStudyEnv?.studyEnvironmentConfig.enableInPersonKits

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
        </div>
        <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
          <Link to={'/hub/kits/in-person'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
            Complete a kit in-person
          </Link>
        </div>
      </>}
    <h3>Your kits ({visibleKitRequests.length})</h3>
    <div className="d-flex flex-column">
      {visibleKitRequests.length === 0 ? (
        <div className="text-center text-muted fst-italic my-4">
          You do not have any sample collection kits at this time.
        </div>
      ) : (
        visibleKitRequests.map((kit, index) => (
          <div key={index}
            className="d-flex align-items-center justify-content-between mb-3 border rounded-3 p-3">
            <div>
              <div className="fw-bold">{kit.kitType.displayName} Kit</div>
              <div className="fst-italic text-muted">
                {instantToDateString(kit.createdAt)}
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
