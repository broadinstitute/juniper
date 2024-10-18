import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import React, { useMemo, useState } from 'react'
import { Button } from '../../../components/forms/Button'
import { Modal } from 'react-bootstrap'
import ParticipantMergeView from './ParticipantMergeView'
import { ParticipantUserWithEnrollees } from '../participantList/PortalUserList'
import { instantToDateString } from '@juniper/ui-core'
import _groupBy from 'lodash/groupBy'

export type DupeType = 'username' | 'name'

export type UserDupe = {
  users: ParticipantUserWithEnrollees[]
  dupeType: DupeType
}

export default function useParticipantDupeTab({ users, studyEnvContext, onUpdate }:
  { users: ParticipantUserWithEnrollees[], studyEnvContext: StudyEnvContextT, onUpdate: () => void }) {
  const possibleDupes = useMemo(() => identifyDupes(users), [users])
  const [showMergeModal, setShowMergeModal] = useState(false)
  const [sourceUsername, setSourceUsername] = useState<string>()
  const [targetUsername, setTargetUsername] = useState<string>()
  return {
    name: `Possible Duplicates ${possibleDupes ? `(${possibleDupes.length})` : ''}`,
    component: <div>
      <table>
        <thead>
          <tr>
            <th>usernames</th>
            <th className="px-5">names</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {possibleDupes?.map(dupe => <tr key={`${dupe.users[0].id}${dupe.dupeType}`}
            className="border-bottom border-top">
            <td className="py-3">
              {dupe.users.map((user, index) => <div key={index}>
                {user.username} <span className="fst-italic text-muted">
                  ({instantToDateString(user.createdAt)})
                </span></div>)}
            </td>
            <td className="px-5 py-3">
              {dupe.users.map((user, index) => <div key={index}>
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
          </tr>)}
        </tbody>
      </table>
      {showMergeModal && <Modal show={true} onHide={() => setShowMergeModal(false)} size={'xl'}>
        <Modal.Header closeButton>
          <Modal.Title>
            Merge Users
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <ParticipantMergeView studyEnvContext={studyEnvContext} source={sourceUsername} target={targetUsername}
            onUpdate={onUpdate}/>
        </Modal.Body>
      </Modal>}
    </div>
  }
}


const NO_DATA = 'no_data'
const DUPE_FUNCTIONS: {type: DupeType, func: (user: ParticipantUserWithEnrollees) => string}[] = [
  { type: 'username', func: (user: ParticipantUserWithEnrollees) => user.username.toLowerCase() },
  {
    type: 'name', func: (user: ParticipantUserWithEnrollees) => {
      if (user.enrollees.length === 0 ||
        !user.enrollees[0].profile.givenName && !user.enrollees[0].profile.familyName) {
        return NO_DATA
      }
      return `${user.enrollees[0].profile.givenName?.toLowerCase()} 
          ${user.enrollees[0].profile.familyName?.toLowerCase()}`
    }
  }
]

export function identifyDupes(users: ParticipantUserWithEnrollees[]) {
  const possibleDupes: UserDupe[] = []
  DUPE_FUNCTIONS.forEach(dupeFunc => {
    const dupeGroups = _groupBy(users, dupeFunc.func)
    Object.keys(dupeGroups).forEach(dupeKey => {
      if (dupeGroups[dupeKey].length > 1 && dupeKey !== NO_DATA) {
        possibleDupes.push({
          users: dupeGroups[dupeKey],
          dupeType: dupeFunc.type
        })
      }
    })
  })
  return possibleDupes
}

