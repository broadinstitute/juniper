import { Enrollee } from '../api/api'
import RemainingTasksAlert from './RemainingTasksAlert'
import React from 'react'
import { useActiveUser } from '../providers/ActiveUserProvider'
import { useUser } from '../providers/UserProvider'

/**
 * Item in a dropdown list which allows the user to switch between different participants.
 */
export default function HubPageParticipantSelectorItem(
  {
    enrollee,
    relationshipType
  }: {
    enrollee: Enrollee,
    relationshipType: string | undefined
  }) {
  const { ppUsers } = useUser()

  const { setActiveUser } = useActiveUser()

  const mappingRelationships: Map<string, string> = new Map([
    ['PROXY', 'Your Dependent']
    // in the future, there will be more relationship types, such as parent
  ])


  function getTitle() {
    const name = `${enrollee.profile?.givenName || ''} ${enrollee.profile?.familyName || ''}`
    if (name.trim() !== '') {
      return name
    } else {
      if (!relationshipType) {
        return 'You'
      }

      return mappingRelationships.get(relationshipType)
    }
  }


  return (
    <li>
      <button onClick={() => {
        const ppUser = ppUsers.find(ppUser => ppUser.profileId === enrollee.profileId)
        if (ppUser) {
          setActiveUser(ppUser.id)
        }
      }} className="dropdown-item">
        <span className="me-1">{getTitle()}</span>
        <RemainingTasksAlert enrollee={enrollee}/>
      </button>
    </li>)
}
