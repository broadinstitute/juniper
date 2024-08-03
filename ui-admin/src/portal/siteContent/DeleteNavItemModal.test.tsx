import { userEvent } from '@testing-library/user-event'
import DeleteNavItemModal from './DeleteNavItemModal'
import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockNavbarItem } from 'test-utils/mock-site-content'
import { NavbarItemInternal, setupRouterTest } from '@juniper/ui-core'

describe('DeletePageModal', () => {
  test('Delete button calls delete with the text', async () => {
    //Arrange
    const mockDeleteItemFn = jest.fn()
    const navItem = mockNavbarItem()
    const { RoutedComponent } = setupRouterTest(<DeleteNavItemModal
      onDismiss={jest.fn()}
      deleteNavItem={mockDeleteItemFn}
      navItem={navItem}
    />)
    render(RoutedComponent)

    //Act
    const deleteButton = screen.getByText('Delete')

    await userEvent.click(deleteButton)

    //Assert
    expect(mockDeleteItemFn).toHaveBeenCalledWith(navItem.text)
  })

  test('shows page deletion warning for internal items', async () => {
    //Arrange
    const mockDeleteItemFn = jest.fn()
    const navItem = {
      ...mockNavbarItem(),
      itemType: 'INTERNAL',
      htmlPage: { title: 'test', path: '', sections: [] }
    }
    render(<DeleteNavItemModal
      onDismiss={jest.fn()}
      deleteNavItem={mockDeleteItemFn}
      navItem={navItem as NavbarItemInternal}
    />)
    expect(screen.getByText('This will also delete the page')).toBeInTheDocument()
  })
})
