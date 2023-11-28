import {
  FacetOption,
  FacetValue,
  StableIdStringArrayFacetValue,
  StableIdStringValue
} from 'api/enrolleeSearch'
import Select, { MultiValue } from 'react-select'
import _cloneDeep from 'lodash/cloneDeep'
import React from 'react'

/**
 * renders a facet with a list of stableIds and options for each stableId that will be combined into a single condition
 * for example, a list of surveys and statuses, to enable querying
 * "all participants who have a cardio survey in progress and a basic survey complete"
 */
const StableIdStringArrayFacetView = ({ facetValue, updateValue }: {facetValue: StableIdStringArrayFacetValue,
  updateValue: (facetValue: FacetValue | null) => void}) => {
  /* take a newValue from a react-select update and translate it to an update to the facet value */
  const updateValues = (stableId: string, newValue: MultiValue<FacetOption | undefined>) => {
    const stringArrayVals = newValue ? newValue.map(val => val?.value as string) : []
    let newValues = _cloneDeep(facetValue.values)
    const changedVal = newValues.find(val => val.stableId === stableId)

    if (stringArrayVals.length === 0) {
      // clear out the subValue for this stableId
      newValues = newValues.filter(stableIdVal => stableIdVal.stableId !== stableId)
    } else if (changedVal) {
      // add/remove values from an existing stableId filter
      changedVal.values = stringArrayVals
    } else {
      // create a new filter for a stableId
      newValues.push(new StableIdStringValue(stableId, stringArrayVals))
    }

    if (newValues.length === 0) {
      updateValue(null)
    } else {
      updateValue(new StableIdStringArrayFacetValue(facetValue.facet, { values: newValues }))
    }
  }

  const facet = facetValue.facet
  return <div>
    {facet.entities.map((stableIdOption, index) => {
      const stringValues = facetValue.values.find(val => val.stableId === stableIdOption.value)?.values ?? []
      const optValues = stringValues.map(stringVal => facet.options.find(opt => opt.value === stringVal))
      return <div key={index}><label>
        { stableIdOption.label }
        <Select options={facet.options} value={optValues} isMulti={true}
          onChange={newValue => updateValues(stableIdOption.value, newValue)}/>
      </label></div>
    })}
  </div>
}

export default StableIdStringArrayFacetView
