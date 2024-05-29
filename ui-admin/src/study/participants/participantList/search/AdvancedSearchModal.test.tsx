import React from 'react'
import { mockTaskSearchFacet } from 'test-utils/mocking-utils'
import { getByText, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AdvancedSearchModal from './AdvancedSearchModal'
import { EnrolleeSearchFacet } from 'api/api'
import { setupRouterTest } from '@juniper/ui-core'

describe('AdvanceSearchModal', () => {
  test('displays search facets', async () => {
    const facet: EnrolleeSearchFacet = mockTaskSearchFacet()

    const mockSetSearchStateFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <AdvancedSearchModal onDismiss={jest.fn()} searchState={{
        basicSearch: '',
        minAge: undefined,
        maxAge: undefined,
        sexAtBirth: [],
        tasks: []
      }} setSearchState={mockSetSearchStateFn}/>)
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
    expect(mockSetSearchStateFn).toHaveBeenCalledWith({
      basicSearch: '',
      minAge: undefined,
      maxAge: undefined,
      sexAtBirth: [],
      tasks: [{ task: 'consent', status: 'complete' }]
    })
  })
})
