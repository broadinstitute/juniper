import React, {
  useCallback,
  useEffect,
  useMemo,
  useState
} from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSearch } from '@fortawesome/free-solid-svg-icons'
import { ParticipantSearchState } from 'util/participantSearchUtils'
import { debounce } from 'lodash'
import classNames from 'classnames'

/**
 * renders and manages updates to a string search facet
 * */
const BasicSearch = ({ searchState, setSearchState, disabled = false }: {
  searchState: ParticipantSearchState,
  setSearchState: (searchState: ParticipantSearchState) => void,
  disabled?: boolean
}) => {
  const [searchText, setSearchText] = useState(searchState.keywordSearch)

  useEffect(() => {
    setSearchText(searchState.keywordSearch)
  }, [searchState.keywordSearch])

  const debouncedUpdate = useMemo(
    () => debounce(value => {
      setSearchState({ ...searchState, keywordSearch: value })
    }, 500), [setSearchState]
  )

  // downloading all the participant data is expensive, so debounce the searchbar
  const handleChange = useCallback(debouncedUpdate, [debouncedUpdate])

  return <form className="rounded-5" onSubmit={e => {
    e.preventDefault()
    setSearchState({ ...searchState, keywordSearch: searchText })
  }}
  style={{ border: '1px solid #bbb', backgroundColor: disabled ? '#f7f7f7' : '#fff', padding: '0.25em 0.75em 0em' }}>
    <button type="submit" title="submit search" className={classNames('btn', 'border-0',
      disabled ? 'btn-light' : 'btn-secondary'
    )} disabled={disabled}>
      <FontAwesomeIcon icon={faSearch}/>
    </button>
    <input
      type="text"
      value={searchText}
      size={40}
      disabled={disabled}
      style={{ border: 'none', outline: 'none' }}
      placeholder={'Search by name, email, or shortcode'}
      onChange={e => {
        setSearchText(e.target.value)
        handleChange(e.target.value)
      }}/>
  </form>
}

export default BasicSearch
