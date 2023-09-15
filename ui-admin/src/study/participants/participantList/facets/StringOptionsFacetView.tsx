import { FacetValue, StringFacetValue } from 'api/enrolleeSearch'
import React from 'react'

/** renders a facet which is a set of string values (e.g. a checkbox set for 'sexAtBirth')
 * as a set of multiselect string checkboxes */
const StringOptionsFacetView = ({ facetValue, updateValue }:
                           {facetValue: StringFacetValue,
                             updateValue: (facetValue: FacetValue | null) => void}) => {
  const values = facetValue.values
  /* updates whether a given value is checked */
  const setValue = (value: string, checked: boolean) => {
    let newValues = [...values]
    if (checked && !values.includes(value)) {
      newValues.push(value)
    } else if (!checked) {
      newValues = newValues.filter(val => val !== value)
    }
    updateValue(new StringFacetValue(facetValue.facet, { values: newValues }))
  }
  if (facetValue.facet.type === 'STRING') {
    return <div>Not supported</div>
  }
  return <div>
    {facetValue.facet.options.map(option => {
      const checked = values.includes(option.value)
      return <div key={option.value}><label>
        <input type="checkbox" className="me-2" checked={checked} value={option.value}
          onChange={() => setValue(option.value, !checked)}/>
        {option.label}
      </label></div>
    })}
  </div>
}

export default StringOptionsFacetView
