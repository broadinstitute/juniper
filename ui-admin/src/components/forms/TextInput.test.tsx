import { act, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { TextInput } from './TextInput'

describe('TextInput', () => {
  it('renders a text input with label', () => {
    // Act
    render(<TextInput label="Value" />)

    // Assert
    // Check that label link works with generated ID.
    const input = screen.getByLabelText('Value')
    expect(input).toBeInstanceOf(HTMLInputElement)
    expect(input).toHaveAttribute('type', 'text')
  })

  it('can use a specified ID', () => {
    // Act
    render(<TextInput id="test-input" label="Value" />)

    // Assert
    // Check that label link works with specified ID.
    const input = screen.getByLabelText('Value')
    expect(input).toHaveAttribute('id', 'test-input')
  })

  it('calls onChange with input value', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<TextInput label="Test input" value="" onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Test input')
    await act(() => user.type(input, 'Y'))

    // Assert
    expect(onChange).toHaveBeenCalledWith('Y')
  })

  it('disables input using aria-disabled', async () => {
    // Arrange
    const user = userEvent.setup()
    const onChange = jest.fn()

    // Act
    render(<TextInput disabled label="Test input" value="" onChange={onChange} />)

    // Assert
    const input = screen.getByLabelText('Test input')

    expect(input).not.toHaveAttribute('disabled')
    expect(input).toHaveAttribute('aria-disabled', 'true')
    expect(input).toHaveClass('disabled')

    await act(() => user.type(input, 'Y'))
    expect(onChange).not.toHaveBeenCalled()
  })

  it('renders description', () => {
    // Act
    render(<TextInput description="More information" label="Test input" />)

    // Assert
    const input = screen.getByLabelText('Test input')
    const description = screen.getByText('More information')
    expect(input).toHaveAttribute('aria-describedby', description.id)
  })

  it('renders a required TextInput', async () => {
    // Act
    render(<TextInput label="My required value" value="" required={true}/>)

    // Assert
    const input = screen.getByLabelText('My required value*')
    expect(input).toHaveClass('is-invalid')
  })
})
