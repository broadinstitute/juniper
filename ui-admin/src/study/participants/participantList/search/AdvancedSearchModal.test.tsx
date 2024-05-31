import React from 'react'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { findByText, fireEvent, getByText, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AdvancedSearchModal from './AdvancedSearchModal'
import { setupRouterTest } from '@juniper/ui-core'

describe('AdvanceSearchModal', () => {
  test('displays search facets', async () => {
    const mockSetSearchStateFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(
      <AdvancedSearchModal
        studyEnvContext={mockStudyEnvContext()}
        onDismiss={jest.fn()}
        searchState={{
          basicSearch: '',
          minAge: undefined,
          maxAge: undefined,
          sexAtBirth: [],
          tasks: [],
          latestKitStatus: [],
          custom: ''
        }}
        setSearchState={mockSetSearchStateFn}/>)
    render(RoutedComponent)

    await screen.findAllByText('Keyword')

    expect(screen.getByText('Sex at birth')).toBeInTheDocument()
    expect(screen.getByText('Age')).toBeInTheDocument()
    expect(screen.getByText('Task status')).toBeInTheDocument()

    // expand accordion
    await userEvent.click(screen.getByText('Task status'))

    expect(screen.getByText('Survey number one')).toBeInTheDocument()
    const selectSection: HTMLElement = screen.getByLabelText('Select status for Survey number one')

    await userEvent.click(selectSection)
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

const keyDownEvent = {
  key: 'ArrowDown'
}

const selectOption = async (container: HTMLElement, optionText: string) => {
  const placeholder = getByText(container, 'Select...')
  fireEvent.keyDown(placeholder, keyDownEvent)
  await findByText(container, optionText)
  fireEvent.click(getByText(container, optionText))
}
