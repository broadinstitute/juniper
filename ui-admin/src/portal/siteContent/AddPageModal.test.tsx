import userEvent from '@testing-library/user-event'
import { mockPortalEnvironment } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import AddPageModal from './AddPageModal'
import { render, screen } from '@testing-library/react'
import React from 'react'

describe('AddPageModal', () => {
  test('disables Create button when title and path aren\'t filled out', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<AddPageModal
      show={true}
      setShow={jest.fn()}
      insertNewPage={jest.fn()}
      portalEnv={mockPortalEnvironment('sandbox')}
      portalShortcode={'test'}
    />)

    render(RoutedComponent)

    //Act
    const createButton = screen.getByText('Create')

    //Assert
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when title and path are filled out', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<AddPageModal
      show={true}
      setShow={jest.fn()}
      insertNewPage={jest.fn()}
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
    const { RoutedComponent } = setupRouterTest(<AddPageModal
      show={true}
      setShow={jest.fn()}
      insertNewPage={mockInsertNewPageFn}
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
      title: 'My New Page',
      path: 'newPage',
      sections: []
    })
  })
})
