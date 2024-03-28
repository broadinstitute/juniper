import { Enrollee, Profile } from '../api/api'
import AlertComponent from './AlertComponent'
import React from 'react'
import { useUser } from '../providers/UserProvider'

/**
 *
 */
export default function HubPageParticipantSelector({ enrollee, profile, relationshipType } :
{enrollee: Enrollee, profile: Profile |undefined, relationshipType: string | undefined}) {
  const mappingRelationships: Map<string, string> = new Map([
    ['PROXY', 'Your Dependent'],
    ['PARENT', 'Your Child']
  ])

  const { setActiveEnrollee, setActiveEnrolleeProfile } = useUser()
  const changeActiveUser = (enrollee: Enrollee, profile: Profile | undefined) => {
    setActiveEnrollee(enrollee)
    profile? setActiveEnrolleeProfile(profile) : setActiveEnrolleeProfile(undefined)
  }

  function getRightTitle() {
    if (!enrollee && !profile && !relationshipType) {
      return 'Your Participant\'s'
    }
    const title = (profile &&  (`${profile.givenName || ''}`)) ||
      (relationshipType && mappingRelationships.get(relationshipType)) || ''
    return title !== '' ? `${title}'s` : 'Your Participant\'s'
  }


  return (
    <li>
      <button onClick={() => changeActiveUser(enrollee, profile)} className="dropdown-item">
        <span> {getRightTitle()} Dashboard </span>
        <AlertComponent enrollee={enrollee}/>
      </button>
    </li>)
}
