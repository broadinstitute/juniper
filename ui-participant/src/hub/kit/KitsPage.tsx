import { Enrollee, KitRequest, KitRequestStatus, KitType, useI18n } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBoxesPacking, faCircleCheck, faTruckFast } from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import { Link } from 'react-router-dom'
import { instantToDateString } from 'util/timeUtils'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { usePortalEnv } from 'providers/PortalProvider'

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
  const { i18n } = useI18n()

  return (
    <div className="progress-stacked border-top border-bottom border-start" style={{ height: '50px' }}>
      <ProgressBar width="33%" complete={getStepCompletion(kit, 'PREPARING')}
        icon={faBoxesPacking} stepId={'preparing'} label={i18n('kitsPageStatusPreparing')}/>
      <ProgressBar width="34%" complete={getStepCompletion(kit, 'SHIPPED')}
        icon={faTruckFast} stepId={'shipped'} label={i18n('kitsPageStatusShipped')}/>
      <ProgressBar width="33%" complete={getStepCompletion(kit, 'RETURNED')}
        icon={faCircleCheck} stepId={'returned'} label={i18n('kitsPageStatusReturned')}/>
    </div>
  )
}

const InPersonKitStatusBar = ({ kit }: { kit: KitRequest }) => {
  const { i18n } = useI18n()

  return (
    <div className="progress-stacked" style={{ height: '50px' }}>
      <ProgressBar width="50%" complete={getStepCompletion(kit, 'PREPARING')}
        icon={faBoxesPacking} stepId={'created'} label={i18n('kitsPageStatusCreated')}/>
      <ProgressBar width="50%" complete={getStepCompletion(kit, 'COLLECTED')}
        icon={faCircleCheck} stepId={'collected'} label={i18n('kitsPageStatusCollected')}/>
    </div>
  )
}

const ProgressBar = ({ stepId, icon, label, width, complete }: {
  stepId: string, icon: IconDefinition, label: string, width: string, complete: boolean
}) => {
  return (
    <div className="progress" role="progressbar" style={{ width, height: '50px' }}>
      <div className={`progress-bar border-end ${complete ? '' : 'bg-dark-subtle'}`}
        data-testid={`${stepId}-${complete}`}
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

  const { i18n } = useI18n()

  return <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm px-2">
    <h1 className="pb-3">
      {i18n('kitsPageTitle')}
    </h1>
    <div className="pb-4">
      {i18n('kitsPageDescription')}
    </div>
    { isInPersonKitEnabled &&
      <>
        <h3>
          {i18n('kitsPageInPersonTitle')}
        </h3>
        <div className="pb-2">
          {i18n('kitsPageInPersonDescription')}
        </div>
        <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
          <Link to={'/hub/kits/in-person'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
            {i18n('kitsPageInPersonCompleteButton')}
          </Link>
        </div>
      </>}
    <h3>{i18n('kitsPageYourKitsTitle')} ({visibleKitRequests.length})</h3>
    <div className="d-flex flex-column">
      {visibleKitRequests.length === 0 ? (
        <div className="text-center text-muted fst-italic my-4">
          {i18n('kitsPageNoKits')}
        </div>
      ) : (
        visibleKitRequests.map((kit, index) => (
          <div key={index}
            className="d-flex align-items-center justify-content-between mb-3 border rounded-3 p-3">
            <div>
              <div className="fw-bold">{i18n(kitTypeToI18nKey(kit.kitType))}</div>
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

const kitTypeToI18nKey = (kitType: KitType) => {
  switch (kitType.name) {
    case 'SALIVA':
      return 'kitTypeSaliva'
    case 'BLOOD':
      return 'kitTypeBlood'
    case 'STOOL':
      return 'kitTypeStool'
    default:
      return 'kitTypeUnknown'
  }
}
