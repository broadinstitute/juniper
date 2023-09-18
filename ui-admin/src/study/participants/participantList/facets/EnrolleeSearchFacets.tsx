import React from 'react'

import { Accordion } from 'react-bootstrap'
import _cloneDeep from 'lodash/cloneDeep'
import {
  checkExhaustiveFacetType,
  Facet,
  FacetValue, IntRangeFacetValue,
  newFacetValue, StableIdStringArrayFacetValue, StringFacetValue,
  StringOptionsFacetValue
} from 'api/enrolleeSearch'
import IntRangeFacetView from './IntRangeFacetView'
import StableIdStringFacetView from './StableIdStringFacetView'
import StringOptionsFacetView from './StringOptionsFacetView'
import StringFacetView from './StringFacetView'

type EnrolleeSearchFacetsProps = {
  facets: Facet[]
  facetValues: FacetValue[]
  updateFacetValues: (values: FacetValue[]) => void
}

/**
 *  returns a new array with updates to the value at the given index in the facetValues array.
 *  -1 as an index will add a new value to the array
 * if the facetValue is null, it has the effect of clearing the facetValue from the array (which will cause the
 * facet to revert to default values
 */
export const getUpdatedFacetValues = (facetValue: FacetValue | null, index: number, facetValues: FacetValue[]) => {
  const newValues = _cloneDeep(facetValues)
  if (facetValue === null) {
    newValues.splice(index, 1)
  } else {
    if (index === -1) {
      newValues.push(facetValue)
    } else {
      newValues[index] = facetValue
    }
  }
  return newValues
}

/**
 * Renders a list of facets in an accordion.  Takes an array of facet values -- this array should represent only
 * those facets that have user-specified non-default values.
 */
export default function EnrolleeSearchFacets({ facets, facetValues, updateFacetValues }: EnrolleeSearchFacetsProps) {
  const updateFacetValue = (facetValue: FacetValue | null, index: number) => {
    updateFacetValues(getUpdatedFacetValues(facetValue, index, facetValues))
  }

  /** clear all resets everything to defaults */
  const clearAll = () => {
    updateFacetValues(facets.map(facet => newFacetValue(facet)))
  }
  return <div>
    <button className="btn btn-secondary float-end" onClick={clearAll}>Clear all</button>
    <Accordion defaultActiveKey={['0']} alwaysOpen flush>
      {facets.map((facet, index) => {
        const matchedValIndex = facetValues.findIndex(facetValue => facetValue.facet.keyName === facet.keyName &&
          facetValue.facet.category === facet.category)
        // matchedVal will be undefined if there is no user-specified non-default value for the facet.
        // The FacetComponent will then render it as the default value
        const matchedVal = facetValues[matchedValIndex]
        return <Accordion.Item eventKey={index.toString()} key={index}>
          <Accordion.Header>{facet.label}</Accordion.Header>
          <Accordion.Body>
            <FacetView facet={facet} facetValue={matchedVal}
              updateValue={facetValue => updateFacetValue(facetValue, matchedValIndex)}/>
          </Accordion.Body>
        </Accordion.Item>
      })}
    </Accordion>
  </div>
}

type FacetViewProps = {
  facet: Facet,
  facetValue?: FacetValue,
  updateValue: (facetValue: FacetValue | null) => void
}

/**
 * Renders a facet with the appropriate component for the facet type.
 */
export const FacetView = ({ facet, facetValue, updateValue }: FacetViewProps) => {
  const facetType = facet.type
  if (!facetValue) {
    facetValue = newFacetValue(facet)
  }
  if (facetType === 'INT_RANGE') {
    return <IntRangeFacetView facetValue={facetValue as IntRangeFacetValue}
      updateValue={updateValue}/>
  } else if (facetType === 'STRING') {
    return <StringFacetView facetValue={facetValue as StringFacetValue}
      updateValue={updateValue}/>
  } else if (facetType === 'STRING_OPTIONS') {
    return <StringOptionsFacetView facetValue={facetValue as StringOptionsFacetValue}
      updateValue={updateValue}/>
  } else if (facetType === 'STABLEID_STRING') {
    return <StableIdStringFacetView facetValue={facetValue as StableIdStringArrayFacetValue}
      updateValue={updateValue}/>
  }
  return checkExhaustiveFacetType(facetType, <></>)
}

