import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTimes } from '@fortawesome/free-solid-svg-icons'
import { ParticipantSearchState } from './ParticipantSearch'

/**
 * Provides a view of the current search criteria showing the facets and values that have been selected,
 * and allowing the user to delete criteria.
 */
const SearchCriteriaView = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  const handleDelete = (label: string) => {
    if (label === 'Age') {
      updateSearchState('minAge', undefined)
      updateSearchState('maxAge', undefined)
    }

    if (label === 'Sex at birth') {
      updateSearchState('sexAtBirth', [])
    }

    // technically, task names could conflict with another facet label (e.g., Age), but it's unlikely
    if (searchState.tasks.findIndex(task => task.task === label) !== -1) {
      updateSearchState('tasks', searchState.tasks.filter(task => task.task !== label))
    }

    if (label === 'Latest Kit') {
      updateSearchState('latestKitStatus', [])
    }

    if (label === 'Expression') {
      updateSearchState('custom', '')
    }
  }

  const getFacets = (searchState: ParticipantSearchState): { label: string, value: string }[] => {
    const facets = []

    if (searchState.minAge && searchState.maxAge) {
      facets.push({ label: 'Age', value: `${searchState.minAge} to ${searchState.maxAge}` })
    } else if (searchState.minAge) {
      facets.push({ label: 'Age', value: `>= ${searchState.minAge}` })
    } else if (searchState.maxAge) {
      facets.push({ label: 'Age', value: `<= ${searchState.maxAge}` })
    }

    if (searchState.sexAtBirth.length > 0) {
      facets.push({ label: 'Sex at birth', value: searchState.sexAtBirth.join(', ') })
    }

    if (searchState.tasks) {
      searchState.tasks.forEach(task => {
        facets.push({ label: task.task, value: task.status })
      })
    }

    if (searchState.latestKitStatus.length > 0) {
      facets.push({ label: 'Latest Kit', value: searchState.latestKitStatus.join(', ') })
    }

    if (searchState.custom) {
      facets.push({ label: 'Expression', value: searchState.custom })
    }

    return facets
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
