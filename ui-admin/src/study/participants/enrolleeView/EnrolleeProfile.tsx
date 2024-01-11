import React, { useState } from 'react'
import { Enrollee, MailingAddress, Profile } from 'api/api'
import { dateToDefaultString } from 'util/timeUtils'
import ParticipantNotesView from './ParticipantNotesView'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'

/**
 * shows the enrollee profile and allows editing from the admin side
 */
export default function EnrolleeProfile({ enrollee, studyEnvContext, onUpdate }:
{enrollee: Enrollee, studyEnvContext: StudyEnvContextT, onUpdate: () => void}) {
  const [profile, setProfile] = useState<Profile>(enrollee.profile)
  const [editMode, setEditMode] = useState<boolean>(false)

  const updateMailingAddress = (mailingAddress: MailingAddress) => {
    // use callback to avoid infinitely recursive updates
    setProfile((oldVal: Profile) => {
      return {
        ...oldVal,
        mailingAddress
      }
    })
  }

  return <div>
    <input type={'checkbox'}
      checked={editMode}
      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEditMode(e.target.checked)}
    />
    <form className="mb-3">
      <div>
        <label className="form-label">
          Given name: <input className="form-control" type="text" readOnly={editMode}
            value={enrollee.profile.givenName}/>
        </label>
        <label className="form-label ms-2">
          Family name: <input className="form-control" type="text" readOnly={editMode}
            value={enrollee.profile.familyName}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Contact email:
          <input className="form-control" type="text" readOnly={editMode} value={enrollee.profile.contactEmail}/>
        </label>
        <label className="form-label ms-3">
          Do not email:
          <input type="checkbox" className="ms-1" disabled={editMode} checked={enrollee.profile.doNotEmail}/>
        </label>
        <label className="form-label ms-3">
          Do not solicit:
          <input type="checkbox" className="ms-1" disabled={editMode} checked={enrollee.profile.doNotEmailSolicit}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Birthdate:
          <input className="form-control" type="text"
            readOnly={editMode} value={dateToDefaultString(enrollee.profile.birthDate)}/>
        </label>
      </div>
      <div className="mb-3">
        <label className="form-label">
          Phone:
          <input className="form-control" type="text" readOnly={editMode} value={enrollee.profile.phoneNumber}/>
        </label>
      </div>
      <h3 className="h6">Mailing address</h3>
      {enrollee.profile.mailingAddress &&
          <MailingAddressView mailingAddress={enrollee.profile.mailingAddress} editMode={editMode}
            updateMailingAddress={updateMailingAddress}/>}
      { !enrollee.profile.mailingAddress && <span className="detail">none</span>}
    </form>
    <ParticipantNotesView notes={enrollee.participantNotes} enrollee={enrollee}
      studyEnvContext={studyEnvContext} onUpdate={onUpdate}/>
  </div>
}

/** displays the mailing list -- does not yet support editing */
export function MailingAddressView(
  { mailingAddress, editMode, updateMailingAddress }: {
    mailingAddress: MailingAddress,
    editMode: boolean,
    updateMailingAddress: (val: MailingAddress) => void
  }
) {
  const onFieldChange = (field: string, value: string) => {
    updateMailingAddress({
      ...mailingAddress,
      [field]: value
    })
  }

  return <div>
    <div>
      <label className="form-label">
        Street 1: <input className="form-control" type="text" readOnly={editMode} value={mailingAddress.street1}/>
      </label>
    </div><div>
      <label className="form-label">
        Street 2: <input className="form-control" type="text" readOnly={editMode} value={mailingAddress.street2}/>
      </label>
    </div><div>
      <label className="form-label">
        City: <input className="form-control" type="text" readOnly={editMode} value={mailingAddress.city}/>
      </label>
      <label className="form-label ms-2">
        State: <input className="form-control" type="text" readOnly={editMode} value={mailingAddress.state}/></label>
      <label className="form-label ms-2">
        Country: <input className="form-control" type="text" readOnly={editMode}
          value={mailingAddress.country}/></label>
      <label className="form-label">
        Postal code: <input className="form-control" type="text" readOnly={editMode} value={mailingAddress.postalCode}/>
      </label>
    </div>
  </div>
}

