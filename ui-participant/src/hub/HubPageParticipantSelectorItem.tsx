import { Enrollee } from '../api/api'
import RemainingTasksAlert from './RemainingTasksAlert'
import React from 'react'
import { useI18n } from '@juniper/ui-core'

/**
 * Item in a dropdown list which allows the user to switch between different participants.
 */
export default function HubPageParticipantSelectorItem(
  {
    enrollee,
    relationshipType,
    setActiveEnrollee
  }: {
    enrollee: Enrollee,
    relationshipType: string | undefined,
    setActiveEnrollee: React.Dispatch<React.SetStateAction<Enrollee | undefined>>
  }) {
  const { i18n } = useI18n()

  // TODO: add yourDependent and you keys
  const mappingRelationships: Map<string, string> = new Map([
    ['PROXY', i18n('yourDependent')]
    // in the future, there will be more relationship types, such as parent
  ])


  function getTitle() {
    if (!relationshipType) {
      return i18n('you')
    }

    const name = `${enrollee.profile?.givenName || ''} ${enrollee.profile?.familyName || ''}`
    if (name.trim() !== '') {
      return name
    } else {
      return mappingRelationships.get(relationshipType)
    }
  }


  return (
    <li>
      <button onClick={() => setActiveEnrollee(enrollee)} className="dropdown-item">
        <span className="me-1">{getTitle()}</span>
        <RemainingTasksAlert enrollee={enrollee}/>
      </button>
    </li>)
}
