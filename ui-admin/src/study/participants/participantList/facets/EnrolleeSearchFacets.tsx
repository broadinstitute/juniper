import React from 'react'

import { Accordion } from 'react-bootstrap'
import { toNumber } from 'lodash'
import { ParticipantSearchState } from '../search/ParticipantSearch'

/**
 * Renders a list of facets in an accordion.  todo
 */
export default function EnrolleeSearchFacets({ searchState, updateSearchState, reset }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void,
  reset: () => void
}) {
  return <div>
    <button className="btn btn-secondary float-end" onClick={reset}>Clear all</button>
    <Accordion alwaysOpen flush>
      <Accordion.Item eventKey={'keyword'} key={'keyword'}>
        <Accordion.Header>Keyword</Accordion.Header>
        <Accordion.Body>
          <KeywordFacet searchState={searchState} updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
      <Accordion.Item eventKey={'age'} key={'age'}>
        <Accordion.Header>Age</Accordion.Header>
        <Accordion.Body>
          <AgeFacet searchState={searchState} updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
      <Accordion.Item eventKey={'sexAtBirth'} key={'sexAtBirth'}>
        <Accordion.Header>Sex at birth</Accordion.Header>
        <Accordion.Body>
          <SexAssignedAtBirthFacet searchState={searchState} updateSearchState={updateSearchState}/>
        </Accordion.Body>
      </Accordion.Item>
    </Accordion>
  </div>
}

const KeywordFacet = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  return <div>
    <div>
      <input className='form-control' type="text" value={searchState.basicSearch || ''}
        onChange={e => updateSearchState('basicSearch', e.target.value)}/>
    </div>
  </div>
}

const AgeFacet = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  return <div>
    <label>Age</label>
    <div>
      <input className='form-control' type="text" value={searchState.minAge || ''}
        onChange={e => updateSearchState('minAge', toNumber(e.target.value))}/>
      <span> to </span>
      <input className='form-control' type="text" value={searchState.maxAge || ''}
        onChange={e => updateSearchState('maxAge', toNumber(e.target.value))}/>
    </div>
  </div>
}


const SexAssignedAtBirthFacet = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  const toggleFacetForAssignedSex = (assignedSex: string) => {
    if (isFilteredByAssignedSex(assignedSex)) {
      updateSearchState('sexAtBirth', searchState.sexAtBirth.filter(s => s !== assignedSex))
    } else {
      updateSearchState('sexAtBirth', [...searchState.sexAtBirth, assignedSex])
    }
  }

  const isFilteredByAssignedSex = (assignedSex: string) => {
    return searchState.sexAtBirth.filter(s => s === assignedSex).length > 0
  }

  return <div>
    <label
      className='form-label'>
      Female
      <input
        className='form-check-input'
        type="checkbox"
        checked={isFilteredByAssignedSex('F')}
        onChange={() => toggleFacetForAssignedSex('F')}/>
    </label>
  </div>
}
