import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import React, { useState } from 'react'
import { Button } from '../../../components/forms/Button'
import { Modal } from 'react-bootstrap'
import ParticipantMergeView from './ParticipantMergeView'
import { ParticipantUserWithEnrollees } from '../participantList/PortalUserList'
import { instantToDateString } from '@juniper/ui-core'

export type DupeType = 'username' | 'name'

export type UserDupe = {
  users: ParticipantUserWithEnrollees[]
  dupeType: DupeType
}

export default function ParticipantDupeView({ possibleDupes, studyEnvContext, onUpdate }:
  { possibleDupes: UserDupe[], studyEnvContext: StudyEnvContextT, onUpdate: () => void }) {
  const [showMergeModal, setShowMergeModal] = useState(false)
  const [sourceUsername, setSourceUsername] = useState<string>()
  const [targetUsername, setTargetUsername] = useState<string>()
  return <div>
    <table>
      <thead>
        <tr>
          <th>usernames</th>
          <th className="px-5">names</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        { possibleDupes?.map(dupe => <tr key={`${dupe.users[0].id}${dupe.dupeType}`}
          className="border-bottom border-top">
          <td className="py-3">
            {dupe.users.map(user => <div>
              {user.username} <span className="fst-italic text-muted">
                ({instantToDateString(user.createdAt)})
              </span></div>)}
          </td>
          <td className="px-5 py-3">
            {dupe.users.map(user => <div>
              {user.enrollees[0] ?
              `${user.enrollees[0].profile.givenName} ${user.enrollees[0].profile.familyName}` :
                <span className="text-muted fst-italic">not provided</span>}
            </div>)}
          </td>
          <td className="py-3">
            <Button variant="secondary" outline={true} onClick={() => {
              setSourceUsername(dupe.users[0].username)
              setTargetUsername(dupe.users[1].username)
              setShowMergeModal(true)
            }}>Preview Merge</Button>
          </td>
        </tr>) }
      </tbody>
    </table>
    { showMergeModal && <Modal show={true} onHide={() => setShowMergeModal(false)} size={'xl'}>
      <Modal.Header closeButton>
        <Modal.Title>
          Merge Users
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <ParticipantMergeView studyEnvContext={studyEnvContext} source={sourceUsername} target={targetUsername}
          onUpdate={onUpdate}/>
      </Modal.Body>
    </Modal> }
  </div>
}
