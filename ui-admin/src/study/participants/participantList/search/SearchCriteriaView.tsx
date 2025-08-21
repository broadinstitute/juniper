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
const SearchCriteriaView = ({ searchState, updateSearchState, customLabels }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void,
  customLabels?: { [index: string]: string }
}) => {
  const handleDelete = (labelToDelete: string) => {
    // technically, task names could conflict with another facet label (e.g., Age), but it's unlikely
    if (searchState.tasks.findIndex(task => task.task === labelToDelete) !== -1) {
      updateSearchState('tasks', searchState.tasks.filter(task => task.task !== labelToDelete))
    } else {
      for (const [field, label] of Object.entries(ParticipantSearchStateLabels)) {
        const customLabel = customLabels?.[field]
        if (label === labelToDelete || customLabel === labelToDelete) {
          updateSearchState(field as keyof ParticipantSearchState,
            DefaultParticipantSearchState[field as keyof ParticipantSearchState])
          return
        }
      }
    }
  }

  const advancedSearchFacets = getFacets(searchState, { customLabels })

  if (advancedSearchFacets.length === 0) {
    return <></>
  }

  return (
    <div className="d-flex flex-wrap gap-2">
      {advancedSearchFacets.map(f => {
        if (f.label.startsWith('includeFields')) {
          return null // don't list includeFields as facets -- they're dynamic columns
        }
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
