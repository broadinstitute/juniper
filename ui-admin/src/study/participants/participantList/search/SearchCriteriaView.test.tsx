import React from 'react'
import { mockTaskSearchFacet, mockTaskFacetValue, mockOptionsFacetValue } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { EnrolleeSearchFacet } from 'api/api'
import { FacetValue, SEX_FACET, EntityOptionsArrayFacet } from 'api/enrolleeSearch'
import SearchCriteriaView from './SearchCriteriaView'

describe('SearchCriteriaView', () => {
  test('shows and deletes search criteria', async () => {
    const facet: EnrolleeSearchFacet = mockTaskSearchFacet()
    const taskFacetValue = mockTaskFacetValue(facet as EntityOptionsArrayFacet, 'COMPLETE')
    const sexFacetValue = mockOptionsFacetValue(SEX_FACET, 'Female')

    const facetValues: FacetValue[] = [taskFacetValue, sexFacetValue]

    const mockUpdateFacetValuesFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <SearchCriteriaView facetValues={facetValues}
        updateFacetValues={mockUpdateFacetValuesFn} />)
    render(RoutedComponent)

    expect(screen.getByText('Sex at birth: Female')).toBeInTheDocument()
    expect(screen.getByText(content => content.startsWith('Task status'))).toBeInTheDocument()

    const deleteIcons = screen.getAllByTestId('CancelIcon')
    expect(deleteIcons).toHaveLength(2)
    await userEvent.click(deleteIcons[0])

    expect(mockUpdateFacetValuesFn).toHaveBeenCalledWith([sexFacetValue])
  })
})
