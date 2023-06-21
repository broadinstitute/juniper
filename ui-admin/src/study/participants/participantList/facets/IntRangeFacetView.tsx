import { FacetValue, IntRangeFacetValue } from 'api/enrolleeSearch'
import React from 'react'

/**
 *
 */
const IntRangeFacetView = ({ facetValue, updateValue }:
                             {facetValue: IntRangeFacetValue,
                               updateValue: (facetValue: FacetValue | null) => void}) => {
  const min = facetValue.min ?? ''
  const max = facetValue.max ?? ''
  const textToVal = (text: string): number | null => {
    const intVal = parseInt(text)
    if (isNaN(intVal)) {
      return null
    }
    return intVal
  }
  return <div>
    <label>min <input type="text" size={2} value={min}
      onChange={e => updateValue(new IntRangeFacetValue(
        facetValue.facet, {
          min: textToVal(e.target.value),
          max: facetValue.max
        }))}/></label>
    <label className="ms-3">max <input type="text" size={2} value={max}
      onChange={e => updateValue(new IntRangeFacetValue(
        facetValue.facet, {
          min: facetValue.min,
          max: textToVal(e.target.value)
        }))}/></label>
  </div>
}

export default IntRangeFacetView
