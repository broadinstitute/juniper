import { KEYWORD_FACET, newFacetValue, StringFacetValue } from 'api/enrolleeSearch'
import React, { useEffect, useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSearch } from '@fortawesome/free-solid-svg-icons'

/**
 * renders and manages updates to a string search facet
 * */
const BasicSearch = ({ facetValue, updateValue }:
                           { facetValue: StringFacetValue,
                             updateValue: (facetValue: StringFacetValue | null) => void
                           }) => {
  if (!facetValue) {
    facetValue = newFacetValue(KEYWORD_FACET) as StringFacetValue
  }
  const valueString = facetValue.values.join(', ')
  const [keywordFieldValue, setKeywordFieldValue] = useState(valueString)

  useEffect(() => {
    setKeywordFieldValue(valueString)
  }, [valueString])

  const updateKeyword = (keyword: string) => {
    const newValues = keyword?.split(/[ ,]+/) ?? []
    updateValue(new StringFacetValue(facetValue.facet, { values: newValues }))
  }

  return <form className="rounded-5" onSubmit={e => {
    e.preventDefault()
    updateKeyword(keywordFieldValue)
  }} style={{ border: '1px solid #bbb', backgroundColor: '#fff', padding: '0.25em 0.75em 0em' }}>
    <button type="submit" title="submit search" className="btn btn-secondary">
      <FontAwesomeIcon icon={faSearch}/>
    </button>
    <input type="text" value={keywordFieldValue} size={40}
      title={facetValue.facet.title}
      style={{ border: 'none', outline: 'none' }}
      placeholder={facetValue.facet.placeholder}
      onChange={e => setKeywordFieldValue(e.target.value)}/>
  </form>
}

export default BasicSearch
