import React from 'react'
import { Enrollee, MailingAddress } from 'api/api'
import { dateToDefaultString } from 'util/timeUtils'
import ParticipantNotesView from './ParticipantNotesView'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'

/**
 * shows the enrollee profile.  Designed for read-only.  When we implement admin-profile editing capability,
 * we should do it via a survey so we can reuse that editing and snapshot infrastructure
 */
export default function EnrolleeProfile({ enrollee, studyEnvContext, onUpdate }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  return <div>
    <form className="mb-3">
      <div>
        <label className="form-label">
          Given name: <input className="form-control" type="text" readOnly={true} value={enrollee.profile.givenName}/>
        </label>
        <label className="form-label ms-2">
          Family name: <input className="form-control" type="text" readOnly={true} value={enrollee.profile.familyName}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Contact email:
          <input className="form-control" type="text" readOnly={true} value={enrollee.profile.contactEmail}/>
        </label>
        <label className="form-label ms-3">
          Do not email:
          <input type="checkbox" className="ms-1" disabled={true} checked={enrollee.profile.doNotEmail}/>
        </label>
        <label className="form-label ms-3">
          Do not solicit:
          <input type="checkbox" className="ms-1" disabled={true} checked={enrollee.profile.doNotEmailSolicit}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Birthdate:
          <input className="form-control" type="text"
            readOnly={true} value={dateToDefaultString(enrollee.profile.birthDate)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Phone:
          <input className="form-control" type="text" readOnly={true} value={enrollee.profile.phoneNumber}/>
        </label>
      </div>
      <h3 className="h6">Mailing address</h3>
      { enrollee.profile.mailingAddress && <MailingAddressView mailingAddress={enrollee.profile.mailingAddress}/>}
      { !enrollee.profile.mailingAddress && <span className="detail">none</span>}
    </form>
    <ParticipantNotesView notes={enrollee.participantNotes} enrollee={enrollee}
      studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
  </div>
}

/** displays the mailing list -- does not yet support editing */
export function MailingAddressView({ mailingAddress }: {mailingAddress: MailingAddress}) {
  return <div>
    <div>
      <label className="form-label">
        Street 1: <input className="form-control" type="text" readOnly={true} value={mailingAddress.street1}/>
      </label>
    </div><div>
      <label className="form-label">
        Street 2: <input className="form-control" type="text" readOnly={true} value={mailingAddress.street2}/>
      </label>
    </div><div>
      <label className="form-label">
        City: <input className="form-control" type="text" readOnly={true} value={mailingAddress.city}/>
      </label>
      <label className="form-label ms-2">
        State: <input className="form-control" type="text" readOnly={true} value={mailingAddress.state}/></label>
      <label className="form-label ms-2">
        Country: <input className="form-control" type="text" readOnly={true} value={mailingAddress.country}/></label>
      <label className="form-label">
        Postal code: <input className="form-control" type="text" readOnly={true} value={mailingAddress.postalCode}/>
      </label>
    </div>
  </div>
}

