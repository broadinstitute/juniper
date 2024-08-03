
import { render, screen } from '@testing-library/react'
import React from 'react'
import CreateNewStudyModal from './CreateNewStudyModal'
import { userEvent } from '@testing-library/user-event'
import { mockPortal } from '../test-utils/mocking-utils'

describe('CreateNewStudyModal', () => {
  test('enables Create button when survey name and stable ID are filled out', async () => {
    const user = userEvent.setup()
    render(<CreateNewStudyModal onDismiss={jest.fn()} portal={mockPortal()}/>)

    const nameInput = screen.getByLabelText('Study Name')
    const stableIdInput = screen.getByLabelText('Study Shortcode')
    expect(screen.getByText('Create')).toHaveAttribute('aria-disabled', 'true')
    await user.type(nameInput, 'Test study')
    await user.type(stableIdInput, 'teststudy')

    expect(screen.getByText('Create')).toHaveAttribute('aria-disabled', 'false')
  })
})
