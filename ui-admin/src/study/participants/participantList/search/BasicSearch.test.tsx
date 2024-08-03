import React from 'react'
import {
  render,
  screen,
  waitFor
} from '@testing-library/react'
import BasicSearch from './BasicSearch'
import { userEvent } from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'
import { DefaultParticipantSearchState } from 'util/participantSearchUtils'

describe('keywordSearch', () => {
  test('can specify keyword facet value', async () => {
    const setSearchState = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <BasicSearch searchState={DefaultParticipantSearchState}
        setSearchState={setSearchState}/>)
    render(RoutedComponent)

    const searchBox = screen.getByPlaceholderText('Search by name, email, or shortcode')
    expect(searchBox).toBeInTheDocument()
    await userEvent.type(searchBox, 'test')
    await waitFor(
      () => expect(setSearchState).toHaveBeenCalledWith({
        'custom': '',
        'keywordSearch': 'test',
        'latestKitStatus': [],
        'sexAtBirth': [],
        'subject': true,
        'tasks': []
      })
    )

    await userEvent.clear(searchBox)
    await waitFor(
      () => expect(setSearchState).toHaveBeenCalledWith({
        'custom': '',
        'keywordSearch': '',
        'latestKitStatus': [],
        'sexAtBirth': [],
        'subject': true,
        'tasks': []
      })
    )
  })
})
