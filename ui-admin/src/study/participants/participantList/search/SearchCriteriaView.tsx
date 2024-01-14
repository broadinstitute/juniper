import React from 'react'
import {
  FacetValue,
  IntRangeFacetValueFields,
  EntityOptionsArrayFacetValueFields,
  StringFacetValueFields
} from 'api/enrolleeSearch'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTimes } from '@fortawesome/free-solid-svg-icons'

/**
 * Provides a view of the current search criteria showing the facets and values that have been selected,
 * and allowing the user to delete criteria.
 */
const SearchCriteriaView = ({ facetValues, updateFacetValues }:
                              { facetValues: FacetValue[], updateFacetValues: (values: FacetValue[]) => void }) => {
  const handleDelete = (deleteFacetValue: FacetValue) => {
    updateFacetValues(facetValues.filter(facetValue =>
      facetValue.facet.keyName !== deleteFacetValue.facet.keyName))
  }

  return (
    <div className="d-flex flex-wrap gap-2">
      {facetValues.filter(facetValue => !facetValue.isDefault()).map(facetValue => {
        return (
          <button
            key={facetValue.facet.keyName}
            className="btn btn-outline-secondary btn-sm btn-light rounded-pill"
            data-testid={'CancelIcon'}
            onClick={() => handleDelete(facetValue)}
          >
            {facetNameAndValueString(facetValue)}
            <FontAwesomeIcon icon={faTimes} className="ms-2"/>
          </button>
        )
      })}
    </div>
  )
}

/**
 * Converts a facet value into a string for display in the form: "Facet Label: value"
 */
const facetNameAndValueString = (facetValue: FacetValue): string => {
  const facet = facetValue.facet
  const facetLabel = facet.label

  const facetType = facet.facetType
  let value = ''
  if (facetType === 'INT_RANGE') {
    const intValue = facetValue as IntRangeFacetValueFields
    value = `${intValue.min ?? '0'} to ${intValue.max ?? ''}`
  } else if (facetType === 'ENTITY_OPTIONS') {
    if (facetValue) {
      value = (facetValue as EntityOptionsArrayFacetValueFields).values.map(entityToValues => {
        const entityLabel = facet.entities.find(entity => entity.value === entityToValues.stableId)?.label ?? ''
        const optionVals = entityToValues.values.map(val => facet.options.find(opt => opt.value === val)?.label ?? '')
        return `${entityLabel}: ${optionVals.join(', ')}`
      }).join('; ')
    }
  } else if (facetType === 'STRING_OPTIONS' || facetType === 'STRING') {
    const stringValue = facetValue as StringFacetValueFields
    value = stringValue.values.join(', ')
  }

  return `${facetLabel}: ${value}`
}

export default SearchCriteriaView
