import userEvent from '@testing-library/user-event'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import DeletePageModal from './DeletePageModal'
import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockHtmlPage } from 'test-utils/mock-site-content'

describe('DeletePageModal', () => {
  test('disables Delete button when confirm string is not typed in', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<DeletePageModal
      onDismiss={jest.fn()}
      deletePage={jest.fn()}
      renderedPage={mockHtmlPage()}
    />)

    render(RoutedComponent)

    //Assert
    const deleteButton = screen.getByText('Delete')
    expect(deleteButton).toBeDisabled()
  })

  test('enables Delete button when confirm string is typed in', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<DeletePageModal
      onDismiss={jest.fn()}
      deletePage={jest.fn()}
      renderedPage={mockHtmlPage()}
    />)

    render(RoutedComponent)

    //Act
    const confirmDeleteInput = screen.getByLabelText('Confirm by typing "delete example page" below.')
    const deleteButton = screen.getByText('Delete')

    await userEvent.type(confirmDeleteInput, 'delete example page')

    //Assert
    expect(deleteButton).toBeEnabled()
  })

  test('Delete button calls deletePage with the page', async () => {
    //Arrange
    const mockDeletePageFn = jest.fn()
    const mockPage = mockHtmlPage()
    const { RoutedComponent } = setupRouterTest(<DeletePageModal
      onDismiss={jest.fn()}
      deletePage={mockDeletePageFn}
      renderedPage={mockPage}
    />)

    render(RoutedComponent)

    //Act
    const confirmDeleteInput = screen.getByLabelText('Confirm by typing "delete example page" below.')
    const deleteButton = screen.getByText('Delete')

    await userEvent.type(confirmDeleteInput, 'delete example page')
    await userEvent.click(deleteButton)

    //Assert
    expect(mockDeletePageFn).toHaveBeenCalledWith(mockPage)
  })
})
