import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import BasicSearch from './BasicSearch'
import userEvent from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'
import { DefaultParticipantSearchState } from 'util/participantSearchUtils'

describe('keywordSearch', () => {
  test('can specify keyword facet value', async () => {
    const updateSearchState = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <BasicSearch searchState={DefaultParticipantSearchState}
        updateSearchState={updateSearchState}/>)
    render(RoutedComponent)

    const searchBox = screen.getByPlaceholderText('Search by name, email, or shortcode')
    expect(searchBox).toBeInTheDocument()
    await userEvent.type(searchBox, 'test{enter}')
    await waitFor(
      () => expect(updateSearchState).toHaveBeenCalledWith('keywordSearch', 'test')
    )

    await userEvent.clear(searchBox)
    await userEvent.type(searchBox, '{enter}')
    await waitFor(
      () => expect(updateSearchState).toHaveBeenCalledWith('keywordSearch', '')
    )
  })
})
