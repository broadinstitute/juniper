import React, { useState } from 'react'
import { FacetValue, facetNameAndValue } from '../../../../api/enrolleeSearch'

import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'

/**
 * Implements a modal dialog for specifying specific search criteria for the participant list.
 */
const SearchCriteriaView = ({ facetValues, updateFacetValues }:
                               { facetValues: FacetValue[],
                                 updateFacetValues: (values: FacetValue[]) => void,
                               }) => {
  const [localFacets, setLocalFacets] = useState(facetValues)

  const updateLocalFacets = (facetValues: FacetValue[]) => {
    setLocalFacets(facetValues)
  }

  const handleDelete = (deleteFacet: FacetValue) => {
    // probably need to match on category too
    updateFacetValues(facetValues.filter(facet => facet.facet.keyName !== deleteFacet.facet.keyName))
  }

  return (
    <Stack direction="row" spacing={1}>
      {facetValues.filter(facetValue => !facetValue.isDefault()).map(facetValue => {
        return (
          <Chip
            key={facetValue.facet.keyName}
            label={facetNameAndValue(facetValue)}
            onDelete={() => handleDelete(facetValue)}
          />
        )
      })}
    </Stack>
  )
}

export default SearchCriteriaView
