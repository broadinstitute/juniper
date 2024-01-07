import React from 'react'
import { FacetValue, facetNameAndValueString } from 'api/enrolleeSearch'
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
            className="btn btn-outline-secondary btn-sm rounded-pill"
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

export default SearchCriteriaView
