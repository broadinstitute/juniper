import React, {useState} from 'react'

import {Accordion, Card} from 'react-bootstrap'
import _cloneDeep from "lodash/cloneDeep";
type FacetValue = object
type StringFacetValue = FacetValue & {
  values: string[]
}
type IntRangeFacetValue = FacetValue & {
  min: number | null,
  max: number | null
}

type FacetType = | 'INT_RANGE' | 'STRING' | 'STABLEID_STRING'

type Facet = {
  keyName: string,
  category: string,
  label: string,
  type: FacetType
}

type IntRangeFacet = Facet & {
  type: 'INT_RANGE'
  min: number | null,
  max: number | null
}

type FacetOption = {
  label: string, value: string
}

type StringFacet = Facet & {
  type: 'STRING',
  options: FacetOption[]
}

type StableIdStringFacet = Facet & {
  type: 'STABLEID_STRING',
  options: FacetOption[]
  stableIdOptions: FacetOption[]
}

const facets: (IntRangeFacet | StringFacet | StableIdStringFacet)[] = [{
  category: 'profile',
  keyName: 'age',
  label: 'Age',
  type: 'INT_RANGE',
  max: 150,
  min: 0
}, {
  category: 'profile',
  keyName: 'sexAtBirth',
  label: 'Sex at birth',
  type: 'STRING',
  options: [
    {value: 'male', label: 'male'},
    {value: 'female', label: 'female'},
    {value: 'other', label: 'other'},
    {value: 'unknown', label: 'unknown'}
  ]
}, {
  category: 'participantTask',
  keyName: 'status',
  label: 'Tasks',
  type: 'STABLEID_STRING',
  options: [
    {value: 'COMPLETE', label: 'Complete'},
    {value: 'IN_PROGRESS', label: 'In progress'},
    {value: 'NEW', label: 'New'}
  ],
  stableIdOptions: [
    {value: 'oh_oh_consent', label: 'Consent'},
    {value: 'oh_oh_basicInfo', label: 'Basics'},
    {value: 'oh_oh_cardioHx', label: 'Cardio History'}
  ]
}]

export default function EnrolleeSearchFacets() {
  const initialValues = () => facets.map(facet => {
    if (facet.type === 'STRING') {
      return {values: []} as FacetValue
    } else if (facet.type === 'INT_RANGE') {
      return {max: null, min: null} as FacetValue
    }
    return {values: []} as FacetValue
  })
  const [facetValues, setFacetValues] = useState<FacetValue[]>(initialValues())
  const updateFacetValue = (facetValue: FacetValue, index: number) => {
    const newValues = _cloneDeep(facetValues)
    newValues[index] = facetValue
    setFacetValues(newValues)
  }
  return <div>
    <button className="btn btn-secondary">Clear all</button>
    <Accordion defaultActiveKey={['0']} alwaysOpen flush>
      {facets.map((facet, index) => <Accordion.Item eventKey={index.toString()}>
        <Accordion.Header>{facet.label}</Accordion.Header>
        <Accordion.Body>
          <FacetView facet={facet} facetValue={facetValues[index]}
                     updateValue={(facetValue) => updateFacetValue(facetValue, index)}/>
        </Accordion.Body>
      </Accordion.Item>)}
    </Accordion>
  </div>
}

const FacetView = ({facet, facetValue, updateValue}:
                     {facet: Facet, facetValue: FacetValue, updateValue: (facetValue: FacetValue) => void}) => {
  if (facet.type === 'INT_RANGE') {
    return <IntRangeFacetView facet={facet as IntRangeFacet} facetValue={facetValue as IntRangeFacetValue}
    updateValue={updateValue as ((facetValue: IntRangeFacetValue) => void)}/>
  } else if (facet.type === 'STRING') {
    return <StringFacetView facet={facet as StringFacet} facetValue={facetValue as StringFacetValue}
                              updateValue={updateValue as ((facetValue: StringFacetValue) => void)}/>
  }
  return <span>yo</span>
}

const IntRangeFacetView = ({facet, facetValue, updateValue}:
                             {facet: IntRangeFacet, facetValue: IntRangeFacetValue,
  updateValue: (facetValue: IntRangeFacetValue) => void}) => {
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
                      onChange={e => updateValue({
                        max: facetValue.max,
                        min: textToVal(e.target.value)
                      })}/></label> -
    <label>max <input type="text" size={2} value={max}
                      onChange={e => updateValue({
                        max: textToVal(e.target.value),
                        min: facetValue.max
                      })}/></label>
  </div>
}

const StringFacetView = ({facet, facetValue, updateValue}:
                           {facet: StringFacet, facetValue: StringFacetValue,
                             updateValue: (facetValue: StringFacetValue) => void}) => {
  const values = facetValue.values
  const setValue = (value: string, checked: boolean ) => {
    let newValues = [...values]
    if (checked && !values.includes(value)) {
      newValues.push(value)
    } else if (!checked) {
      newValues = newValues.filter(val => val !== value)
    }
    updateValue({values: newValues})
  }
  return <div>
    {facet.options.map(option => {
      const checked = values.includes(option.value)
      return <div><label>
          <input type="checkbox" className="me-2" checked={checked} value={option.value}
                 onChange={() => setValue(option.value, !checked)}/>
          {option.label}
        </label></div>
    })}
  </div>
}
