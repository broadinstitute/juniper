import React from 'react'
import { mockPortal } from 'test-utils/mocking-utils'
import  { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import DeleteUserModal from './DeleteUserModal'
import userEvent from '@testing-library/user-event'
import { mockAdminUser } from '../test-utils/user-mocking-utils'

describe('DeleteUserModal', () => {
  test('enables Remove button when user completes matching confirmation string', async () => {
    //Arrange
    const user = userEvent.setup()
    const portal = mockPortal()
    const subjUser = mockAdminUser(false)
    subjUser.username = 'user@email'

    const { RoutedComponent } = setupRouterTest(<DeleteUserModal
      subjUser={subjUser}
      portal={portal}
      userDeleted={jest.fn()}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    //Assert page content
    const confirmRemoveUserInput = screen.getByText('Confirm by typing "remove user@email" below')
    const removeButton = screen.getByText('Remove user')
    expect(removeButton).toBeDisabled()

    //Act
    await user.type(confirmRemoveUserInput, 'remove user@email')

    //Assert action
    expect(removeButton).toBeEnabled()
  })
})
