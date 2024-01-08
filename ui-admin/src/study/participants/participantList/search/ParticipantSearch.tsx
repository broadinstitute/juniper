import React, { useState } from 'react'
import { getUpdatedFacetValues } from '../facets/EnrolleeSearchFacets'
import {
  Facet,
  FacetValue,
  StringFacetValue
} from 'api/enrolleeSearch'
import { Button } from 'components/forms/Button'
import AdvancedSearchModal from './AdvancedSearchModal'
import SearchCriteriaView from './SearchCriteriaView'
import BasicSearch from './BasicSearch'


/** Participant search component for participant list page */
function ParticipantSearch({ facets, facetValues, updateFacetValues }: {
                            facets: Facet[], facetValues: FacetValue[],
                            updateFacetValues: (values: FacetValue[]) => void}) {
  const [advancedSearch, setAdvancedSearch] = useState(false)

  const keywordFacetIndex = facetValues.findIndex(facet => facet.facet.category === 'keyword')
  const keywordFacetValue = facetValues[keywordFacetIndex]

  const updateKeywordFacetValue = (facetValue: StringFacetValue | null) => {
    updateFacetValues(getUpdatedFacetValues(facetValue ?? null, keywordFacetIndex, facetValues))
  }

  const hasFacetValues = (): boolean => {
    return facetValues.length > 0
  }

  return <div>
    <div className="align-items-baseline d-flex mb-2">
      {advancedSearch && <AdvancedSearchModal onDismiss={() => setAdvancedSearch(false)} facetValues={facetValues}
        updateFacetValues={updateFacetValues} searchCriteria={facets}/>}
      <div className="mb-2">
        <BasicSearch facetValue={keywordFacetValue as StringFacetValue} updateValue={updateKeywordFacetValue}/>
      </div>
      <div className="ms-2">
        <Button variant="light" className="border btn-sm"
          onClick={() => setAdvancedSearch(true)}>
        Advanced Search
        </Button>
      </div>
    </div>
    {hasFacetValues() && <div className="d-flex mb-4">
      <SearchCriteriaView facetValues={facetValues} updateFacetValues={updateFacetValues}/>
    </div>}
  </div>
}

export default ParticipantSearch
