import React from 'react'
import { screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import SearchCriteriaView from './SearchCriteriaView'
import { DefaultParticipantSearchState } from 'util/participantSearchUtils'
import { renderWithRouterAndStore } from 'test-utils/mocking-utils'

describe('SearchCriteriaView', () => {
  test('shows and deletes search criteria', async () => {
    const mockUpdateSearchStateFn = jest.fn()
    renderWithRouterAndStore(
      <SearchCriteriaView searchState={{
        ...DefaultParticipantSearchState,
        sexAtBirth: ['F'],
        tasks: [{ task: 'consent', status: 'complete' }]
      }} updateSearchState={mockUpdateSearchStateFn}/>)

    expect(screen.getByText('Sex at birth: F')).toBeInTheDocument()
    expect(screen.getByText(content => content.startsWith('consent:'))).toBeInTheDocument()

    const deleteIcons = screen.getAllByTestId('CancelIcon')
    expect(deleteIcons).toHaveLength(2)
    await userEvent.click(deleteIcons[0])

    expect(mockUpdateSearchStateFn).toHaveBeenCalledWith('sexAtBirth', [])
  })
})
