import React from 'react'
import { Link } from 'react-router-dom'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { Enrollee, KitRequest } from '@juniper/ui-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBox, faCircleExclamation, faRefresh } from '@fortawesome/free-solid-svg-icons'
import QRCode from 'react-qr-code'

//TODO: JN-1294, implement i18n for this entire component
export default function KitInstructions() {
  const { ppUser, enrollees } = useActiveUser()
  const activeEnrollee = enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)

  return <div
    className="hub-dashboard-background flex-grow-1"
    style={{ background: 'var(--dashboard-background-color)' }}>
    <div className="row mx-0 justify-content-center">
      <div className="my-md-4 mx-auto" style={{ maxWidth: 768 }}>
        <div className="card-body">
          <div className="align-items-center">
            <BaseKitInstructions/>
            {activeEnrollee ?
              <KitContent enrollee={activeEnrollee}/> :
              <div className="text-danger">
                No enrollee found. Please contact a member of the study team for assistance.
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  </div>
}

const BaseKitInstructions = () => {
  return <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm">
    <h1 className="pb-3">Sample Kit Instructions</h1>
    <div className="pb-3">
      If you are completing a sample collection kit in-person, please follow the instructions provided
        by a member of the study team. Any additional information that you may need, such as your unique
        participant identifier, will be provided below.
    </div>
    <div className="pb-3">
      If you have any questions, please ask a member of the study team.
    </div>
  </div>
}

const KitContent = ({ enrollee }: { enrollee: Enrollee }) => {
  const activeKit = enrollee.kitRequests.filter(kit => kit.distributionMethod === 'IN_PERSON')[0]

  if (!enrollee.consented) { return <UnconsentedKitView/> }
  if (!activeKit) { return <NoActiveKitView enrollee={enrollee}/> }
  if (activeKit.status === 'COLLECTED_BY_STAFF') { return <CollectedKitView/> }

  return <DistributedKitView enrollee={enrollee} activeKit={activeKit}/>
}

const UnconsentedKitView = () => {
  return (
    <div className="mb-3 rounded round-3 p-3 bg-white shadow-sm">
      <h2 className="d-flex align-items-center mb-3">
        <FontAwesomeIcon className="text-danger me-2" icon={faCircleExclamation}/> Consent Required
      </h2>
      <div className="mb-3">
        Before completing a sample collection kit, you must read and sign the study consent form.
      </div>
      <div className="d-flex justify-content-center">
        <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
          Start Consent
        </Link>
      </div>
    </div>
  )
}

const NoActiveKitView = ({ enrollee }: { enrollee: Enrollee }) => {
  return (
    <div className="mb-3 rounded round-3 py-4 px-md-5 bg-white shadow-sm">
      <h2 className="d-flex align-items-center mb-3">
        <FontAwesomeIcon className="me-2" icon={faBox}/> Sample collection kit
      </h2>
      <div>
          To receive a sample collection kit, a member of the study team will scan your unique participation code
          below to associate a sample kit with your account.
      </div>
      <div className="d-flex flex-column align-items-center">
        <QRCode value={enrollee.shortcode} size={300}
          className={'m-5 p-4 border rounded-3 shadow-lg'} aria-label={'assign-qr'}/>
      </div>
      <div className="pb-3">
          Once you have received a kit, please refresh this page to view your kit information and
          receive further instructions.
      </div>
      <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
        <button className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary"
          onClick={() => window.location.reload()}>
          <FontAwesomeIcon icon={faRefresh}/> Refresh
        </button>
      </div>
    </div>
  )
}

const CollectedKitView = () => {
  return (
    <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm">
      <h2 className="d-flex align-items-center mb-3">
        <FontAwesomeIcon className="me-2" icon={faBox}/> Sample collection kit
      </h2>
      <div className="mb-3">
          A member of the study team has received your sample collection kit.
          You will receive an email notification when your sample has been processed, and you will be able
          to view the status of the sample on your study dashboard.
      </div>
      <div className="mb-3">
          Thank you for your participation.
      </div>
      <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
        <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
          Return to Dashboard
        </Link>
      </div>
    </div>
  )
}

const DistributedKitView = ({ enrollee, activeKit }: { enrollee: Enrollee, activeKit: KitRequest }) => {
  return (
    <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm px-md-5">
      <h2 className="d-flex align-items-center mb-3">
        <FontAwesomeIcon className="me-2" icon={faBox}/> Sample collection kit
      </h2>
      <div className="mb-3">
          A member of the team has provided you with a sample collection kit.
          This sample kit is associated with your account. If you are assisting someone else with their
          sample collection kit, please ensure that each participant completes the sample collection
          kit that was assigned to them.
      </div>
      <label className="form-label fw-bold mb-0">Your kit identifier:</label>
      <input
        className="mb-2 form-control bg-white"
        disabled={true}
        placeholder={'No kit provided'}
        value={activeKit.kitBarcode}>
      </input>
      <div className="mt-3">
          After you have completed the sample collection kit, please return it to a member of the study team
          and allow them to scan your participation code below.
      </div>
      <div className="d-flex flex-column align-items-center">
        <QRCode value={enrollee.shortcode} size={300}
          className={'m-5 p-4 border rounded-3 shadow-lg'} aria-label={'return-qr'}/>
      </div>
      <div className="py-3 text-center mb-4" style={{ background: 'var(--brand-color-shift-90)' }}>
        <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
          Return to Dashboard
        </Link>
      </div>
    </div>
  )
}
