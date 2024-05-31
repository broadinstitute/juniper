import React, { useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSearch } from '@fortawesome/free-solid-svg-icons'
import { ParticipantSearchState } from './ParticipantSearch'

/**
 * renders and manages updates to a string search facet
 * */
const BasicSearch = ({ searchState, updateSearchState }: {
  searchState: ParticipantSearchState,
  updateSearchState: (field: keyof ParticipantSearchState, value: unknown) => void
}) => {
  const [search, setSearch] = useState(searchState.basicSearch)
  return <form className="rounded-5" onSubmit={e => {
    e.preventDefault()
    updateSearchState('basicSearch', search)
  }} style={{ border: '1px solid #bbb', backgroundColor: '#fff', padding: '0.25em 0.75em 0em' }}>
    <button type="submit" title="submit search" className="btn btn-secondary">
      <FontAwesomeIcon icon={faSearch}/>
    </button>
    <input type="text" value={search} size={40}
      style={{ border: 'none', outline: 'none' }}
      placeholder={'Search by name, email, or shortcode'}
      onChange={e => setSearch(e.target.value)}/>
  </form>
}

export default BasicSearch
