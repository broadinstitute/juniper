import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import EnrolleeSearchFacets from '../facets/EnrolleeSearchFacets'
import { ParticipantSearchState } from './ParticipantSearch'
import { StudyEnvContextT } from '../../../StudyEnvironmentRouter'

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
    setLocalSearchState({
      basicSearch: '',
      sexAtBirth: [],
      tasks: []
    })
  }

  return <Modal show={true} onHide={onDismiss}>
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
