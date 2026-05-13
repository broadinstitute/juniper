import React from 'react'
import {
  screen,
  waitFor
} from '@testing-library/react'
import BasicSearch from './BasicSearch'
import { userEvent } from '@testing-library/user-event'
import { DefaultParticipantSearchState } from 'util/participantSearchUtils'
import { renderWithRouterAndStore } from 'test-utils/mocking-utils'

describe('keywordSearch', () => {
  test('can specify keyword facet value', async () => {
    const setSearchState = jest.fn()
    renderWithRouterAndStore(<BasicSearch searchState={DefaultParticipantSearchState}
      setSearchState={setSearchState}/>)

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
        'tasks': [],
        'fileDownloads': [],
        'fileUploads': [],
        'includeFacetKeys': []
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
        'tasks': [],
        'fileDownloads': [],
        'fileUploads': [],
        'includeFacetKeys': []
      })
    )
  })
})
