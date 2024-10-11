import React from 'react'
import { Link } from 'react-router-dom'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { Enrollee, KitRequest, useI18n } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircleExclamation, faRefresh } from '@fortawesome/free-solid-svg-icons'
import { EnrolleeShortcodeQR } from './EnrolleeShortcodeQR'

//TODO: JN-1294, implement i18n for this entire component
export default function KitInstructions() {
  const { ppUser, enrollees } = useActiveUser()
  const activeEnrollee = enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)
  const { i18n } = useI18n()

  return <div
    className="hub-dashboard-background flex-grow-1"
    style={{ background: 'var(--dashboard-background-color)' }}>
    <div className="row mx-0 justify-content-center">
      <div className="my-md-4 mx-auto px-0" style={{ maxWidth: 768 }}>
        <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm px-2">
          <h1 className="pb-2">{i18n('kitsInPersonTitle')}</h1>
          <div className="pb-3">
            {i18n('kitsInPersonDescription')}
          </div>
          <div className="pb-4">
            {i18n('kitsInPersonSubDescription')}
          </div>
          {activeEnrollee ?
            <KitContent enrollee={activeEnrollee}/> :
            <div className="text-danger">
              {i18n('kitsInPersonError')}
            </div>
          }
        </div>
      </div>
    </div>
  </div>
}

const KitContent = ({ enrollee }: { enrollee: Enrollee }) => {
  //in the event that there are multiple in-person kits for an enrollee, we want to display the most recent one.
  //the assumption is that there will only ever be one, but in case study staff have to reissue a kit, we want to
  //display the most recent one
  const activeKit = enrollee.kitRequests.filter(kit => kit.distributionMethod === 'IN_PERSON')
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())[0]

  if (!enrollee.consented) {
    return <UnconsentedKitView/>
  }
  if (!activeKit) {
    return <NoActiveKitView enrollee={enrollee}/>
  }
  if (activeKit.status === 'COLLECTED_BY_STAFF') {
    return <CollectedKitView/>
  }

  return <DistributedKitView enrollee={enrollee} activeKit={activeKit}/>
}

const UnconsentedKitView = () => {
  const { i18n } = useI18n()
  return (<>
    <h2 className="d-flex align-items-center mb-3">
      <FontAwesomeIcon className="text-danger me-2" icon={faCircleExclamation}/>
      {i18n('kitsInPersonConsentRequiredTitle')}
    </h2>
    <div className="pb-3">
      {i18n('kitsInPersonConsentRequiredDescription')}
    </div>
    <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
      <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
        {i18n('kitsInPersonStartConsent')}
      </Link>
    </div>
  </>
  )
}

const NoActiveKitView = ({ enrollee }: { enrollee: Enrollee }) => {
  const { i18n } = useI18n()
  return (
    <>
      <h3 className="d-flex align-items-center mb-2">
        {i18n('kitsPageInPersonTitle')}
      </h3>
      <div>
        {i18n('kitsInPersonNoKitInstructions')}
      </div>
      <EnrolleeShortcodeQR shortcode={enrollee.shortcode}/>
      <div className="pb-3">
        {i18n('kitsInPersonNoKitSubInstructions')}
      </div>
      <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
        <button className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary"
          onClick={() => window.location.reload()}>
          <FontAwesomeIcon className={'me-2'} icon={faRefresh}/>{i18n('kitsInPersonRefreshPage')}
        </button>
      </div>
    </>
  )
}

const CollectedKitView = () => {
  const { i18n } = useI18n()
  return (
    <>
      <h2 className="d-flex align-items-center mb-2">
        {i18n('kitsInPersonYourKitTitle')}
      </h2>
      <div className="mb-3">
        {i18n('kitsInPersonCollectedDescription')}
      </div>
      <div className="mb-3">
        {i18n('kitsInPersonCollectedThankYou')}
      </div>
      <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
        <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
          {i18n('kitsInPersonReturnToDashboard')}
        </Link>
      </div>
    </>
  )
}

const DistributedKitView = ({ enrollee, activeKit }: { enrollee: Enrollee, activeKit: KitRequest }) => {
  const { i18n } = useI18n()
  return (
    <>
      <h2 className="d-flex align-items-center mb-2">
        {i18n('kitsInPersonYourKitTitle')}
      </h2>
      <div className="mb-3">
        {i18n('kitsInPersonYourKitDescription')}
      </div>
      <label className="form-label fw-bold mb-0">{i18n('kitsInPersonYourKitIdentifier')}:</label>
      <input
        className="mb-2 form-control bg-white"
        disabled={true}
        placeholder={'No kit provided'}
        value={activeKit.kitLabel}>
      </input>
      <div className="mt-3">
        {i18n('kitsInPersonCreatedInstructions')}
      </div>
      <EnrolleeShortcodeQR shortcode={enrollee.shortcode}/>
      <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
        <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
          {i18n('kitsInPersonReturnToDashboard')}
        </Link>
      </div>
    </>
  )
}
