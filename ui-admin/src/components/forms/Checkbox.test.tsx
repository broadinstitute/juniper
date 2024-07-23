import { act, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { Checkbox } from './Checkbox'

describe('Checkbox', () => {
  it('renders a checkbox with label', () => {
    // Act
    render(<Checkbox checked label="Value" />)

    // Assert
    // Check that label link works with generated ID.
    const checkbox = screen.getByLabelText('Value')
    expect(checkbox).toBeInstanceOf(HTMLInputElement)
    expect(checkbox).toHaveAttribute('type', 'checkbox')
  })

  it('can use a specified ID', () => {
    // Act
    render(<Checkbox checked id="test-checkbox" label="Value" />)

    // Assert
    // Check that label link works with specified ID.
    const checkbox = screen.getByLabelText('Value')
    expect(checkbox).toHaveAttribute('id', 'test-checkbox')
  })

  it('calls onChange with checked value', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<Checkbox checked label="Test checkbox" value="" onChange={onChange} />)

    // Act
    const checkbox = screen.getByLabelText('Test checkbox')
    await user.click(checkbox)

    // Assert
    expect(onChange).toHaveBeenCalledWith(false)
  })

  it('disables checkbox using aria-disabled', async () => {
    // Arrange
    const user = userEvent.setup()
    const onChange = jest.fn()

    // Act
    render(<Checkbox checked disabled label="Test checkbox" value="" onChange={onChange} />)

    // Assert
    const checkbox = screen.getByLabelText('Test checkbox')

    expect(checkbox).not.toHaveAttribute('disabled')
    expect(checkbox).toHaveAttribute('aria-disabled', 'true')
    expect(checkbox).toHaveClass('disabled')

    await act(() => user.click(checkbox))
    expect(onChange).not.toHaveBeenCalled()
  })

  it('renders description', () => {
    // Act
    render(<Checkbox checked  description="More information" label="Test checkbox" />)

    // Assert
    const checkbox = screen.getByLabelText('Test checkbox')
    const description = screen.getByText('More information')
    expect(checkbox).toHaveAttribute('aria-describedby', description.id)
  })
})
