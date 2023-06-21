import React from 'react'

import {Accordion} from 'react-bootstrap'
import _cloneDeep from "lodash/cloneDeep";
import Select, {MultiValue} from 'react-select'
import {
  Facet,
  FacetOption,
  FacetValue, IntRangeFacetValue,
  newFacetValue, StableIdStringArrayFacetValue,
  StableIdStringValue,
  StringFacetValue
} from "api/enrolleeSearch";


export default function EnrolleeSearchFacets({facets, facetValues, updateFacetValues}:
    {facets: Facet[], facetValues: FacetValue[], updateFacetValues: (values: FacetValue[]) => void}) {

  const updateFacetValue = (facetValue: FacetValue | null, index: number) => {
    let newValues = _cloneDeep(facetValues)
    if (facetValue === null) {
      newValues = newValues.splice(index, index)
    } else {
      if (index === -1) {
        newValues.push(facetValue)
      } else {
        newValues[index] = facetValue
      }
    }
    updateFacetValues(newValues)
  }

  const clearAll = () => {
    updateFacetValues(facets.map(facet => newFacetValue(facet)))
  }
  return <div>
    <button className="btn btn-secondary float-end" onClick={clearAll}>Clear all</button>
    <Accordion defaultActiveKey={['0']} alwaysOpen flush>
      {facets.map((facet, index) => {
        const matchedValIndex = facetValues.findIndex(facetValue => facetValue.facet.keyName === facet.keyName &&
          facetValue.facet.category === facet.category)
        const matchedVal = facetValues[matchedValIndex]
        return <Accordion.Item eventKey={index.toString()} key={index}>
          <Accordion.Header>{facet.label}</Accordion.Header>
          <Accordion.Body>
            <FacetView facet={facet} facetValue={matchedVal}
                       updateValue={(facetValue) => updateFacetValue(facetValue, matchedValIndex)}/>
          </Accordion.Body>
        </Accordion.Item>
      })}
    </Accordion>
  </div>
}

const FacetView = ({facet, facetValue, updateValue}:
                     {facet: Facet, facetValue: FacetValue | undefined,
                       updateValue: (facetValue: FacetValue | null) => void}) => {
  if (!facetValue) {
    facetValue = newFacetValue(facet)
  }
  if (facet.type === 'INT_RANGE') {
    return <IntRangeFacetView facetValue={facetValue as IntRangeFacetValue}
                              updateValue={updateValue}/>
  } else if (facet.type === 'STRING') {
    return <StringFacetView facetValue={facetValue as StringFacetValue}
                              updateValue={updateValue}/>
  } else if (facet.type === 'STABLEID_STRING') {
    return <StableIdStringFacetView facetValue={facetValue as StableIdStringArrayFacetValue}
                                    updateValue={updateValue}/>
  }
  return <span>yo</span>
}

const IntRangeFacetView = ({facetValue, updateValue}:
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

const StringFacetView = ({facetValue, updateValue}:
                           {facetValue: StringFacetValue, updateValue: (facetValue: FacetValue | null) => void}) => {
  const values = facetValue.values
  const setValue = (value: string, checked: boolean ) => {
    let newValues = [...values]
    if (checked && !values.includes(value)) {
      newValues.push(value)
    } else if (!checked) {
      newValues = newValues.filter(val => val !== value)
    }
    updateValue(new StringFacetValue(facetValue.facet, {values: newValues}))
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

const StableIdStringFacetView = ({facetValue, updateValue}: {facetValue: StableIdStringArrayFacetValue,
                             updateValue: (facetValue: FacetValue | null) => void}) => {
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
      updateValue(new StableIdStringArrayFacetValue(facetValue.facet, {values: newValues}))
    }
  }

  const facet = facetValue.facet
  return <div>
    {facet.stableIdOptions.map((stableIdOption, index) => {
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
