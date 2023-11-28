import { FacetValue, StringFacetValue } from 'api/enrolleeSearch'
import React, { useState } from 'react'

/**
 * Renders a facet which is a single or set of string values as a text field
 * */
const StringFacetView = ({ facetValue, updateValue }:
                                    {facetValue: StringFacetValue,
                                        updateValue: (facetValue: FacetValue | null) => void}) => {
  const valueString = facetValue.values.join(', ')
  const [keywordFieldValue, setKeywordFieldValue] = useState(valueString)

  const updateKeyword = (keyword: string) => {
    const newValues = keyword?.split(/[ ,]+/) ?? []
    updateValue(new StringFacetValue(facetValue.facet, { values: newValues }))
  }

  return <div>
    <input type="text" value={keywordFieldValue} size={40}
      title={facetValue.facet.title}
      //style={{ border: 'none', outline: 'none' }}
      placeholder={facetValue.facet.placeholder}
      //!! review this onChange -DC
      onChange={e => setKeywordFieldValue(e.target.value)}
      onBlur={e => updateKeyword(e.target.value)}
    />
  </div>
}

export default StringFacetView
