import React, { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { getUpdatedFacetValues } from '../facets/EnrolleeSearchFacets'
import {
  Facet,
  FacetValue,
  facetValuesToString,
  StringFacetValue
} from '../../../../api/enrolleeSearch'
import { Button } from '../../../../components/forms/Button'
import AdvancedSearchModal from './AdvancedSearchModal'
import SearchCriteriaView from './SearchCriteriaView'
import BasicSearch from './BasicSearch'


/** Participant search component for participant list page */
function ParticipantSearch({ facets, facetValues }: {facets: Facet[], facetValues: FacetValue[]}) {
  const [searchParams, setSearchParams] = useSearchParams()
  const [advancedSearch, setAdvancedSearch] = useState(false)

  const keywordFacetIndex = facetValues.findIndex(facet => facet.facet.category === 'keyword')
  const keywordFacetValue = facetValues[keywordFacetIndex]

  const updateFacetValues = (facetValues: FacetValue[]) => {
    searchParams.set('facets', facetValuesToString(facetValues))
    setSearchParams(searchParams)
  }

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
      {!advancedSearch && <div className="mb-2">
        <BasicSearch facetValue={keywordFacetValue as StringFacetValue} updateValue={updateKeywordFacetValue}/>
      </div>}
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
