import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTimes } from '@fortawesome/free-solid-svg-icons'
import {
  DefaultParticipantSearchState,
  getFacets,
  ParticipantSearchState,
  ParticipantSearchStateLabels
} from 'util/participantSearchUtils'

/**
 * Provides a view of the current search criteria showing the facets and values that have been selected,
 * and allowing the user to delete criteria.
 */
const SearchCriteriaView = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  const handleDelete = (label: string) => {
    // technically, task names could conflict with another facet label (e.g., Age), but it's unlikely
    if (searchState.tasks.findIndex(task => task.task === label) !== -1) {
      updateSearchState('tasks', searchState.tasks.filter(task => task.task !== label))
    } else {
      for (const [key, value] of Object.entries(ParticipantSearchStateLabels)) {
        if (value === label) {
          updateSearchState(key as keyof ParticipantSearchState,
            DefaultParticipantSearchState[key as keyof ParticipantSearchState])
          return
        }
      }
    }
  }

  const advancedSearchFacets = getFacets(searchState)

  if (advancedSearchFacets.length === 0) {
    return <></>
  }

  return (
    <div className="d-flex flex-wrap gap-2 mb-4">
      {advancedSearchFacets.map(f => {
        return (
          <button
            key={f.label}
            className="btn btn-outline-secondary btn-sm btn-light rounded-pill"
            data-testid={'CancelIcon'}
            onClick={() => handleDelete(f.label)}
          >
            {f.label}: {f.value}
            <FontAwesomeIcon icon={faTimes} className="ms-2"/>
          </button>
        )
      })}
    </div>
  )
}

export default SearchCriteriaView
