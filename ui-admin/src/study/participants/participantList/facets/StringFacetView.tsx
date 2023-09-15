import { FacetValue, StringFacetValue } from 'api/enrolleeSearch'
import React, { useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSearch } from '@fortawesome/free-solid-svg-icons'

/** renders a facet which is a single or set of string values as a text field */
const StringFacetView = ({ facetValue, updateValue }:
                                    {facetValue: StringFacetValue,
                                        updateValue: (facetValue: FacetValue | null) => void}) => {
  const valueString = facetValue.values.join(', ')
  const [keywordFieldValue, setKeywordFieldValue] = useState(valueString)

  /* updates whether a given value is checked */
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

export default StringFacetView
