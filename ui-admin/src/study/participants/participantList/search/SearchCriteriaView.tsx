import React from 'react'
import { FacetValue, facetNameAndValueString } from '../../../../api/enrolleeSearch'

import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'

/**
 * Provides a view of the current search criteria showing the facets and values that have been selected,
 * and allowing the user to delete criteria.
 */
const SearchCriteriaView = ({ facetValues, updateFacetValues }:
                               { facetValues: FacetValue[],
                                 updateFacetValues: (values: FacetValue[]) => void,
                               }) => {
  const handleDelete = (deleteFacetValue: FacetValue) => {
    updateFacetValues(facetValues.filter(facetValue =>
      facetValue.facet.keyName !== deleteFacetValue.facet.keyName))
  }

  return (
    <Stack direction="row" spacing={1}>
      {facetValues.filter(facetValue => !facetValue.isDefault()).map(facetValue => {
        return (
          <Chip
            key={facetValue.facet.keyName}
            label={facetNameAndValueString(facetValue)}
            onDelete={() => handleDelete(facetValue)}
          />
        )
      })}
    </Stack>
  )
}

export default SearchCriteriaView
