import { Enrollee, Profile } from '../api/api'
import AlertComponent from './AlertComponent'
import React from 'react'
import { useUser } from '../providers/UserProvider'

/**
 *
 */
export default function HubPageParticipantSelector({ enrollee, profile, relationshipType } :
{enrollee: Enrollee, profile: Profile, relationshipType: string | undefined}) {
  const mappingRelationships: Map<string, string> = new Map([
    ['PROXY', 'Dependent'],
    ['PARENT', 'Child']
  ])

  const { setActiveEnrollee, setActiveEnrolleeProfile } = useUser()
  const changeActiveUser = (enrollee: Enrollee, profile: Profile) => {
    setActiveEnrollee(enrollee)
    setActiveEnrolleeProfile(profile)
  }

  function getRightTitle() {
    if (!enrollee && !profile && !relationshipType) {
      return 'Participant'
    }
    return (profile &&  (`${profile.givenName || ''} ${profile.familyName || ''}`)) ||
      (relationshipType && mappingRelationships.get(relationshipType)) || ''
  }


  return (
    <li>
      <button onClick={() => changeActiveUser(enrollee, profile)} className="dropdown-item">
        <span> {getRightTitle()} Dashboard </span>
        <AlertComponent enrollee={enrollee}/>
      </button>
    </li>)
}
