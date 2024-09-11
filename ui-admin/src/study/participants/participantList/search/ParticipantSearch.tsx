import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import AdvancedSearchModal from './AdvancedSearchModal'
import BasicSearch from './BasicSearch'
import SearchCriteriaView from './SearchCriteriaView'
import { ParticipantSearchState } from 'util/participantSearchUtils'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faFilter } from '@fortawesome/free-solid-svg-icons'


/** Participant search component for participant list page */
function ParticipantSearch({ studyEnvContext, searchState, updateSearchState, setSearchState, disabled = false }: {
  studyEnvContext: StudyEnvContextT,
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void,
  setSearchState: (searchState: ParticipantSearchState) => void,
  disabled?: boolean
}) {
  const [advancedSearch, setAdvancedSearch] = useState(false)

  return <>
    <div className="align-items-center d-flex">
      {advancedSearch && <AdvancedSearchModal
        studyEnvContext={studyEnvContext}
        onDismiss={() => setAdvancedSearch(false)}
        searchState={searchState}
        setSearchState={setSearchState}/>}
      <div className="align-items-center">
        <BasicSearch
          disabled={disabled}
          searchState={searchState}
          setSearchState={setSearchState}/>
      </div>
      <div className="ms-2">
        <Button variant="light" className="border btn-sm" disabled={disabled}
          onClick={() => setAdvancedSearch(true)}>
          <FontAwesomeIcon icon={faFilter}/> Search Filters
        </Button>
      </div>
    </div>
    <SearchCriteriaView
      searchState={searchState}
      updateSearchState={updateSearchState}/>
  </>
}

export default ParticipantSearch
