import React from 'react'
import { mockTaskSearchFacet, mockTaskFacetValue } from '../../../../test-utils/mocking-utils'
import { setupRouterTest } from '../../../../test-utils/router-testing-utils'
import { getByText, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AdvancedSearchModal from './AdvancedSearchModal'
import { EnrolleeSearchFacet } from '../../../../api/api'
import { ALL_FACETS, Facet, FacetValue, StableIdStringArrayFacet } from '../../../../api/enrolleeSearch'

describe('AdvanceSearchModal', () => {
  test('displays search facets', async () => {
    const facet: EnrolleeSearchFacet = mockTaskSearchFacet()
    const searchFacets: Facet[] = [...ALL_FACETS, facet as Facet]
    const facetValues: FacetValue[] = [mockTaskFacetValue(facet as StableIdStringArrayFacet, 'COMPLETE')]

    const mockUpdateFacetValuesFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <AdvancedSearchModal onDismiss={jest.fn()} facetValues={facetValues}
        updateFacetValues={mockUpdateFacetValuesFn} searchCriteria={searchFacets}/>)
    render(RoutedComponent)

    await screen.findAllByText('Keyword')

    expect(screen.getByText('Sex at birth')).toBeInTheDocument()
    expect(screen.getByText('Age')).toBeInTheDocument()
    expect(screen.getByText('Task status')).toBeInTheDocument()

    // expand accordion
    await userEvent.click(screen.getByText('Task status'))

    expect(screen.getByText('Consent')).toBeInTheDocument()
    const selectSection: HTMLElement  = screen.getByTestId('select-consent')

    await userEvent.click(getByText(selectSection, 'Complete'))

    // expand accordion
    await userEvent.click(screen.getByText('Sex at birth'))
    expect(screen.getByText('Female')).toBeInTheDocument()

    await userEvent.click(screen.getByText('Search'))
    expect(mockUpdateFacetValuesFn).toHaveBeenCalledWith(facetValues)
  })
})
