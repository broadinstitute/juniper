import { userEvent } from '@testing-library/user-event'
import { mockPortalEnvironment } from 'test-utils/mocking-utils'
import AddNavbarItemModal from './AddNavbarItemModal'
import { render, screen } from '@testing-library/react'
import React from 'react'
import { setupRouterTest } from '@juniper/ui-core'

describe('AddPageModal', () => {
  test('disables Create button when title and path aren\'t filled out', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<AddNavbarItemModal
      onDismiss={jest.fn()}
      insertNewNavItem={jest.fn()}
      portalEnv={mockPortalEnvironment('sandbox')}
      portalShortcode={'test'}
    />)

    render(RoutedComponent)

    //Assert
    const createButton = screen.getByText('Create')
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when title and path are filled out', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<AddNavbarItemModal
      onDismiss={jest.fn()}
      insertNewNavItem={jest.fn()}
      portalEnv={mockPortalEnvironment('sandbox')}
      portalShortcode={'test'}
    />)

    render(RoutedComponent)

    //Act
    const pageTitleInput = screen.getByLabelText('Page Title')
    const pagePathInput = screen.getByLabelText('Page Path')
    const createButton = screen.getByText('Create')

    await userEvent.type(pageTitleInput, 'test')
    await userEvent.type(pagePathInput, 'test')

    //Assert
    expect(createButton).toBeEnabled()
  })

  test('Create button calls insertNewPage with a new page', async () => {
    //Arrange
    const mockInsertNewPageFn = jest.fn()
    const { RoutedComponent } = setupRouterTest(<AddNavbarItemModal
      onDismiss={jest.fn()}
      insertNewNavItem={mockInsertNewPageFn}
      portalEnv={mockPortalEnvironment('sandbox')}
      portalShortcode={'test'}
    />)

    render(RoutedComponent)

    //Act
    const pageTitleInput = screen.getByLabelText('Page Title')
    const pagePathInput = screen.getByLabelText('Page Path')
    const createButton = screen.getByText('Create')

    await userEvent.type(pageTitleInput, 'My New Page')
    await userEvent.type(pagePathInput, 'newPage')
    await userEvent.click(createButton)

    //Assert
    expect(mockInsertNewPageFn).toHaveBeenCalledWith({
      href: 'newPage',
      itemOrder: -1,
      itemType: 'INTERNAL',
      text: 'My New Page'
    })
  })
})
