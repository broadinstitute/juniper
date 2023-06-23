import { FacetValue, IntRangeFacetValue } from 'api/enrolleeSearch'
import React from 'react'

/** converts a text value to either an integer or null */
const textToVal = (text: string): number | null => {
  const intVal = parseInt(text)
  if (isNaN(intVal)) {
    return null
  }
  return intVal
}

/**
 * renders a facet with two text fields for inputting min/max values
 */
const IntRangeFacetView = ({ facetValue, updateValue }:
                             {facetValue: IntRangeFacetValue,
                               updateValue: (facetValue: FacetValue | null) => void}) => {
  const min = facetValue.min ?? ''
  const max = facetValue.max ?? ''

  return <div>
    <label className="me-3">min <input type="number" size={2} value={min} style={{ maxWidth: '5em' }}
      onChange={e => updateValue(new IntRangeFacetValue(
        facetValue.facet, {
          min: textToVal(e.target.value),
          max: facetValue.max
        }))}/></label>
    <label>max <input type="number" size={2} value={max} style={{ maxWidth: '5em' }}
      onChange={e => updateValue(new IntRangeFacetValue(
        facetValue.facet, {
          min: facetValue.min,
          max: textToVal(e.target.value)
        }))}/></label>
  </div>
}

export default IntRangeFacetView
