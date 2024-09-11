import React from 'react'
import { Link } from 'react-router-dom'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { Enrollee, KitRequest, PortalEnvironment } from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faBox, faCircleExclamation, faHourglassStart, faRefresh } from '@fortawesome/free-solid-svg-icons'
import QRCode from 'react-qr-code'

//TODO: JN-1294, implement i18n for this entire component
export default function KitInstructions() {
  const { ppUser, enrollees } = useActiveUser()
  const { portalEnv } = usePortalEnv()
  const activeEnrollee = enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)

  return <div
    className="hub-dashboard-background flex-grow-1"
    style={{ background: 'var(--dashboard-background-color)' }}>
    <div className="row mx-0 justify-content-center py-4">
      <div className="col-12 col-sm-10 col-lg-6">
        <div className="card-body">
          <div className="align-items-center">
            <BaseKitInstructions portalEnv={portalEnv}/>
            {activeEnrollee ?
              <KitContent enrollee={activeEnrollee}/> :
              <div className="text-danger">
                No enrollee found. Please contact a member of the study team.
              </div>
            }
          </div>
        </div>
      </div>
    </div>
  </div>
}

const BaseKitInstructions = ({ portalEnv }: { portalEnv: PortalEnvironment }) => {
  const studySupportEmail = portalEnv.portalEnvironmentConfig.emailSourceAddress

  return <div className="mb-3 rounded round-3 border border-1 p-3 bg-white">
    <h2 className="fw-bold pb-3">Sample Kit Instructions</h2>
    <div className="pb-3">
      If you are receiving a sample collection kit in-person, you will have the option to
      complete the collection kit now and return it immediately, or take it home and ship it
      back using the provided return label.
    </div>
    <div className="pb-3">
      If you have any questions, please ask a member of the
      study team or email <a href={`mailto:${studySupportEmail}`}>{studySupportEmail}</a>
    </div>
  </div>
}

const KitContent = ({ enrollee }: { enrollee: Enrollee }) => {
  const activeKit = enrollee.kitRequests.filter(kit => kit.distributionMethod === 'ASSIGNED')[0]

  if (!enrollee.consented) { return <UnconsentedKitView/> }
  if (!activeKit) { return <NoActiveKitView enrollee={enrollee}/> }
  if (activeKit.status === 'COLLECTED') { return <CollectedKitView/> }

  return <ReturnedKitView enrollee={enrollee} activeKit={activeKit}/>
}

const UnconsentedKitView = () => {
  return (
    <div className="mb-3 rounded round-3 border border-1 p-3 bg-white">
      <h2 className="d-flex align-items-center mb-3">
        <FontAwesomeIcon className="text-danger me-1" icon={faCircleExclamation}/> Consent Required
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
    <div className="mb-3 rounded round-3 border border-1 p-3 bg-white">
      <h2 className="d-flex align-items-center mb-3">
        <FontAwesomeIcon className="me-1" icon={faHourglassStart}/> Your kit information
      </h2>
      <div className="mb-3">
          To receive a sample collection kit, a member of the study team will scan your unique QR code below to
          associate a kit with your account.
      </div>
      <div className="d-flex flex-column align-items-center">
        <QRCode value={enrollee.shortcode} size={200}
          className={'pb-3'} aria-label={'assign-qr'}/>
      </div>
      <div className="pb-3">
          Once you have received a kit, please refresh this page to view the kit information and
          view further instructions.
      </div>
      <div className="d-flex justify-content-center">
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
    <div className="mb-3 rounded round-3 border border-1 p-3 bg-white">
      <h2 className="d-flex align-items-center mb-3">
        <FontAwesomeIcon className="me-1" icon={faBox}/> Your kit information
      </h2>
      <div className="mb-3">
          A member of the study team has received your sample collection kit. Thank you for your participation.
          You will receive an email notification when your sample has been processed, and you will be able
          to view your results on your study dashboard.
      </div>
      <div className="d-flex justify-content-center">
        <Link to={'/hub'} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
            Return to Dashboard
        </Link>
      </div>
    </div>
  )
}

const ReturnedKitView = ({ enrollee, activeKit }: { enrollee: Enrollee, activeKit: KitRequest }) => {
  return (
    <div className="mb-3 rounded round-3 border border-1 p-3 bg-white">
      <h2 className="d-flex align-items-center mb-3">
        <FontAwesomeIcon className="me-1" icon={faBox}/> Your kit information
      </h2>
      <div className="mb-3">
          A member of the team has provided you with a sample collection kit.
          This sample kit is associated with your account. If you are assisting someone else with their
          sample collection kit, please ensure that each participant completes their own sample collection kit.
      </div>
      <label className="form-label fw-bold mb-0">Your unique kit identifier:</label>
      <input
        className="my-2 form-control"
        disabled={true}
        placeholder={'No kit provided'}
        value={activeKit.kitBarcode}>
      </input>
      <div className="mb-3 mt-3">
          After you have completed the sample collection kit, please return it to a member of the study team
          and show them the QR code below.
      </div>
      <div className="d-flex flex-column align-items-center">
        <QRCode value={enrollee.shortcode} size={200}
          className={'pb-3'} aria-label={'return-qr'}/>
      </div>
      <div className="mb-3 mt-3">
          If you wish to complete your sample kit at a later time, you may instead
          choose to return it by shipping it back using the provided return label.
      </div>
    </div>
  )
}
