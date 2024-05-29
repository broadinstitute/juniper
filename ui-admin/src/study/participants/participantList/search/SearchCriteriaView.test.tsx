import React from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SearchCriteriaView from './SearchCriteriaView'
import { setupRouterTest } from '@juniper/ui-core'

describe('SearchCriteriaView', () => {
  test('shows and deletes search criteria', async () => {
    const mockUpdateSearchStateFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <SearchCriteriaView searchState={{
        basicSearch: '',
        minAge: undefined,
        maxAge: undefined,
        sexAtBirth: ['F'],
        tasks: [{ task: 'consent', status: 'complete' }],
        latestKitStatus: [],
        custom: ''
      }} updateSearchState={mockUpdateSearchStateFn}/>)
    render(RoutedComponent)

    expect(screen.getByText('Sex at birth: F')).toBeInTheDocument()
    expect(screen.getByText(content => content.startsWith('Task status'))).toBeInTheDocument()

    const deleteIcons = screen.getAllByTestId('CancelIcon')
    expect(deleteIcons).toHaveLength(2)
    await userEvent.click(deleteIcons[0])

    expect(mockUpdateSearchStateFn).toHaveBeenCalledWith('age', undefined)
  })
})
