import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import AdvancedSearchModal from './AdvancedSearchModal'
import BasicSearch from './BasicSearch'
import SearchCriteriaView from './SearchCriteriaView'
import { ParticipantSearchState } from 'util/participantSearchUtils'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'


/** Participant search component for participant list page */
function ParticipantSearch({ studyEnvContext, searchState, updateSearchState, setSearchState }: {
  studyEnvContext: StudyEnvContextT,
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void,
  setSearchState: (searchState: ParticipantSearchState) => void
}) {
  const [advancedSearch, setAdvancedSearch] = useState(false)

  return <div>
    <div className="align-items-baseline d-flex mb-2">
      {advancedSearch && <AdvancedSearchModal
        studyEnvContext={studyEnvContext}
        onDismiss={() => setAdvancedSearch(false)}
        searchState={searchState}
        setSearchState={setSearchState}/>}
      <div className="mb-2">
        <BasicSearch
          searchState={searchState}
          updateSearchState={updateSearchState}/>
      </div>
      <div className="ms-2">
        <Button variant="light" className="border btn-sm"
          onClick={() => setAdvancedSearch(true)}>
        Advanced Search
        </Button>
      </div>
    </div>
    <SearchCriteriaView
      searchState={searchState}
      updateSearchState={updateSearchState}/>
  </div>
}

export default ParticipantSearch
