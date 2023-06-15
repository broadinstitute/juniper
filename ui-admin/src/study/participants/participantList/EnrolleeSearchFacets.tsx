import React from 'react'

import {Accordion} from 'react-bootstrap'
import _cloneDeep from "lodash/cloneDeep";
import Select, {MultiValue} from 'react-select'
interface IFacetValue {
  isDefault: () => boolean
  facet: Facet
}
type StringFacetValueFields = { values: string[] }
class StringFacetValue implements IFacetValue {
  values: string[]
  facet: StringFacet
  constructor(facet: StringFacet, facetVal: StringFacetValueFields = {values: []}) {
    this.values = facetVal.values
    this.facet = facet
  }
  isDefault() {
    return this.values.length === 0
  }
}
type IntRangeFacetValueFields = {
  min: number | null
  max: number | null
}
class IntRangeFacetValue implements IFacetValue {
  min: number | null
  max: number | null
  facet: IntRangeFacet
  constructor(facet: IntRangeFacet, facetVal: IntRangeFacetValueFields = {min: null, max: null}) {
    this.min = facetVal.min
    this.max = facetVal.max
    this.facet = facet
  }
  isDefault() {
    return this.max === null && this.min === null
  }
}

class StableIdStringValue {
  stableId: string | null
  values: string[]
  constructor(stableId: string | null = null, values: string[] = []) {
    this.values = values
    this.stableId = stableId
  }
  isDefault() {
    return this.values.length === 0
  }
}
type StableIdStringArrayFacetValueFields = { values: StableIdStringValue[] }
class StableIdStringArrayFacetValue implements IFacetValue {
  values: StableIdStringValue[]
  facet: StableIdStringArrayFacet
  constructor(facet: StableIdStringArrayFacet, facetVal: StableIdStringArrayFacetValueFields = {values: []}) {
    this.values = facetVal.values
    this.facet = facet
  }
  isDefault() {
    return this.values.length === 0 || !this.values.some(val => !val.isDefault())
  }
}

export type FacetValue =  StringFacetValue | IntRangeFacetValue | StableIdStringArrayFacetValue

type FacetType = | 'INT_RANGE' | 'STRING' | 'STABLEID_STRING'

type BaseFacet = {
  keyName: string,
  category: string,
  label: string,
  type: FacetType,
}

type IntRangeFacet = BaseFacet & {
  type: 'INT_RANGE'
  min: number | null
  max: number | null
}

type FacetOption = {
  label: string, value: string
}

type StringFacet = BaseFacet & {
  type: 'STRING',
  options: FacetOption[]
}

type StableIdStringArrayFacet = BaseFacet & {
  type: 'STABLEID_STRING',
  options: FacetOption[]
  stableIdOptions: FacetOption[]
}

export type Facet = StringFacet | StableIdStringArrayFacet | IntRangeFacet

export const SAMPLE_FACETS: Facet[] = [{
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
  label: 'Task status',
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


export const newFacetValue = (facet: Facet, facetValue?: object): FacetValue => {
  if (facet.type === 'INT_RANGE') {
    return new IntRangeFacetValue(facet, facetValue as IntRangeFacetValueFields)
  } else if (facet.type === 'STABLEID_STRING') {
    return new StableIdStringArrayFacetValue(facet, facetValue as StableIdStringArrayFacetValueFields)
  }
  return new StringFacetValue(facet, facetValue as StringFacetValueFields)
}

export default function EnrolleeSearchFacets({facets, facetValues, updateFacetValues}:
    {facets: Facet[], facetValues: FacetValue[], updateFacetValues: (values: FacetValue[]) => void}) {

  const updateFacetValue = (facetValue: FacetValue, index: number) => {
    const newValues = _cloneDeep(facetValues)
    newValues[index] = facetValue
    updateFacetValues(newValues)
  }

  const clearAll = () => {
    updateFacetValues(facets.map(facet => newFacetValue(facet)))
  }
  return <div>
    <button className="btn btn-secondary float-end" onClick={clearAll}>Clear all</button>
    <Accordion defaultActiveKey={['0']} alwaysOpen flush>
      {facets.map((facet, index) => {
        const matchedVal = facetValues.find(facetValue => facetValue.facet.keyName === facet.keyName &&
          facetValue.facet.category === facet.category)
        return <Accordion.Item eventKey={index.toString()}>
          <Accordion.Header>{facet.label}</Accordion.Header>
          <Accordion.Body>
            <FacetView facet={facet} facetValue={matchedVal}
                       updateValue={(facetValue) => updateFacetValue(facetValue, index)}/>
          </Accordion.Body>
        </Accordion.Item>
      })}
    </Accordion>
  </div>
}

const FacetView = ({facet, facetValue, updateValue}:
                     {facet: Facet, facetValue: FacetValue | undefined,
                       updateValue: (facetValue: FacetValue) => void}) => {
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
                             {facetValue: IntRangeFacetValue, updateValue: (facetValue: FacetValue) => void}) => {
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
                           {facetValue: StringFacetValue, updateValue: (facetValue: FacetValue) => void}) => {
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
      return <div><label>
          <input type="checkbox" className="me-2" checked={checked} value={option.value}
                 onChange={() => setValue(option.value, !checked)}/>
          {option.label}
        </label></div>
    })}
  </div>
}

const StableIdStringFacetView = ({facetValue, updateValue}: {facetValue: StableIdStringArrayFacetValue,
                             updateValue: (facetValue: FacetValue) => void}) => {
  const updateValues = (stableId: string, newValue: MultiValue<FacetOption | undefined>) => {
    const stringArrayVals = newValue ? newValue.map(val => val?.value as string) : []
    const newValues = _cloneDeep(facetValue.values)
    const changedVal = newValues.find(val => val.stableId === stableId)
    if (changedVal) {
      changedVal.values = stringArrayVals
    } else {
      newValues.push(new StableIdStringValue(stableId, stringArrayVals))
    }
    updateValue(new StableIdStringArrayFacetValue(facetValue.facet, {values: newValues}))
  }
  const facet = facetValue.facet
  return <div>
    {facet.stableIdOptions.map(stableIdOption => {
      const stringValues = facetValue.values.find(val => val.stableId === stableIdOption.value)?.values ?? []
      const optValues = stringValues.map(stringVal => facet.options.find(opt => opt.value === stringVal))
      return <div><label>
        { stableIdOption.label }
        <Select options={facet.options} value={optValues} isMulti={true}
                onChange={newValue => updateValues(stableIdOption.value, newValue)}/>
      </label></div>
    })}
  </div>
}
