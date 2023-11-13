import userEvent from '@testing-library/user-event'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import DeletePageModal from './DeletePageModal'
import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockHtmlPage } from 'test-utils/mock-site-content'

describe('DeletePageModal', () => {
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
    const deleteButton = screen.getByText('Delete')

    await userEvent.click(deleteButton)

    //Assert
    expect(mockDeletePageFn).toHaveBeenCalledWith(mockPage)
  })
})
