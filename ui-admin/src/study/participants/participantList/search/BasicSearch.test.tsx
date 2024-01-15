import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import {
  KEYWORD_FACET,
  StringFacetValue
} from 'api/enrolleeSearch'
import BasicSearch from './BasicSearch'
import userEvent from '@testing-library/user-event'

describe('BasicSearch', () => {
  test('can specify keyword facet value', async () => {
    const keywordFacetValue = new StringFacetValue(KEYWORD_FACET, { values: [] })

    const mockUpdateKeywordFacetValueFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <BasicSearch facetValue={keywordFacetValue}
        updateValue={mockUpdateKeywordFacetValueFn} />)
    render(RoutedComponent)

    const searchBox = screen.getByPlaceholderText(KEYWORD_FACET.placeholder)
    expect(searchBox).toBeInTheDocument()
    await userEvent.type(searchBox, 'test{enter}')

    const updatedFacetValue = new StringFacetValue(KEYWORD_FACET, { values: ['test'] })
    expect(mockUpdateKeywordFacetValueFn).toHaveBeenLastCalledWith(updatedFacetValue)

    await userEvent.clear(searchBox)
    await userEvent.type(searchBox, '{enter}')
    expect(mockUpdateKeywordFacetValueFn).toHaveBeenLastCalledWith(keywordFacetValue)
  })
})
