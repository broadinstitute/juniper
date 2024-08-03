import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import EnrolleeSearchFacets from '../facets/EnrolleeSearchFacets'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  DefaultParticipantSearchState,
  ParticipantSearchState
} from 'util/participantSearchUtils'

/**
 * Implements a modal dialog for specifying specific search criteria for the participant list.
 */
const AdvancedSearchModal = ({ studyEnvContext, searchState, setSearchState, onDismiss }: {
  studyEnvContext: StudyEnvContextT,
  searchState: ParticipantSearchState,
  setSearchState: (searchState: ParticipantSearchState) => void,
  onDismiss: () => void
}) => {
  const [localSearchState, setLocalSearchState] = useState<ParticipantSearchState>(searchState)


  const searchOnClick = () => {
    setSearchState(localSearchState)
    onDismiss()
  }

  const updateLocalSearchState = (field: keyof ParticipantSearchState, value: unknown) => {
    setLocalSearchState(oldState => {
      return { ...oldState, [field]: value }
    })
  }

  const reset = () => {
    setLocalSearchState(DefaultParticipantSearchState)
  }

  return <Modal show={true} onHide={onDismiss} size={'xl'}>
    <Modal.Header closeButton>
      <Modal.Title>Participant search</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <EnrolleeSearchFacets
          studyEnvContext={studyEnvContext}
          searchState={localSearchState}
          updateSearchState={updateLocalSearchState}
          reset={reset}
        />
      </form>
    </Modal.Body>
    <Modal.Footer>
      <button className="btn btn-primary" onClick={searchOnClick}>Search</button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default AdvancedSearchModal
