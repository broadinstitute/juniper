import React from 'react'

import { Accordion } from 'react-bootstrap'
import _cloneDeep from 'lodash/cloneDeep'
import {
  Facet,
  FacetValue, IntRangeFacetValue,
  newFacetValue, StableIdStringArrayFacetValue,
  StringFacetValue
} from 'api/enrolleeSearch'
import IntRangeFacetView from './IntRangeFacetView'
import StableIdStringFacetView from './StableIdStringFacetView'
import StringFacetView from './StringFacetView'

/**
 * Renders a list of facets in an accordion.  Takes an array of facet values -- this array should represent only
 * those facets that have user-specified non-default values.
 */
export default function EnrolleeSearchFacets({ facets, facetValues, updateFacetValues }:
    {facets: Facet[], facetValues: FacetValue[], updateFacetValues: (values: FacetValue[]) => void}) {
  /**
   *  updates the value at the given index in the facetValue array.  -1 as an index will add a new value to the array
   * if the facetValue is null, it has the effect of clearing the facetValue from the array (which will cause the
   * facet to revert to default values
   */
  const updateFacetValue = (facetValue: FacetValue | null, index: number) => {
    let newValues = _cloneDeep(facetValues)
    if (facetValue === null) {
      newValues.splice(index, 1)
    } else {
      if (index === -1) {
        newValues.push(facetValue)
      } else {
        newValues[index] = facetValue
      }
    }
    updateFacetValues(newValues)
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

/**
 * Renders a facet with the appropriate component for the facet type.
 */
const FacetView = ({ facet, facetValue, updateValue }:
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

