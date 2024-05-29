import React from 'react'
import { render, screen } from '@testing-library/react'
import BasicSearch from './BasicSearch'
import userEvent from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

describe('BasicSearch', () => {
  test('can specify keyword facet value', async () => {
    const mockUpdateKeywordFacetValueFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <BasicSearch searchState={{
        basicSearch: '',
        sexAtBirth: [],
        tasks: [],
        latestKitStatus: [],
        custom: ''
      }}
      updateSearchState={mockUpdateKeywordFacetValueFn}/>)
    render(RoutedComponent)

    const searchBox = screen.getByPlaceholderText('Search by name, email, or shortcode')
    expect(searchBox).toBeInTheDocument()
    await userEvent.type(searchBox, 'test{enter}')

    expect(mockUpdateKeywordFacetValueFn).toHaveBeenLastCalledWith('basicSearch', 'test')

    await userEvent.clear(searchBox)
    await userEvent.type(searchBox, '{enter}')
    expect(mockUpdateKeywordFacetValueFn).toHaveBeenLastCalledWith('basicSearch', '')
  })
})
