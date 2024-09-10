import React from 'react'
import { mockPortal } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import DeleteUserModal from './DeleteUserModal'
import { userEvent } from '@testing-library/user-event'
import { mockAdminUser } from '../test-utils/user-mocking-utils'
import { setupRouterTest } from '@juniper/ui-core'

describe('DeleteUserModal', () => {
  test('enables Remove button when user completes matching confirmation string', async () => {
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

    // verify page content and
    const confirmRemoveUserInput = screen.getByText('Confirm by typing "remove user@email" below')
    const removeButton = screen.getByText('Remove')
    expect(removeButton).toBeDisabled()

    // verify button is enabled when user types matching string
    await user.type(confirmRemoveUserInput, 'remove user@email')
    expect(removeButton).toBeEnabled()
  })
})
