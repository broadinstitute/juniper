import { act, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { Textarea } from './Textarea'

describe('Textarea', () => {
  it('renders a textarea with label', () => {
    // Act
    render(<Textarea label="Value" />)

    // Assert
    // Check that label link works with generated ID.
    const input = screen.getByLabelText('Value')
    expect(input).toBeInstanceOf(HTMLTextAreaElement)
  })

  it('can use a specified ID', () => {
    // Act
    render(<Textarea id="test-input" label="Value" />)

    // Assert
    // Check that label link works with specified ID.
    const input = screen.getByLabelText('Value')
    expect(input).toHaveAttribute('id', 'test-input')
  })

  it('calls onChange with input value', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<Textarea label="Test input" value="" onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Test input')
    await act(() => user.type(input, 'Y'))

    // Assert
    expect(onChange).toHaveBeenCalledWith('Y')
  })

  it('disables textarea using aria-disabled', async () => {
    // Arrange
    const user = userEvent.setup()
    const onChange = jest.fn()

    // Act
    render(<Textarea disabled label="Test input" value="" onChange={onChange} />)

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
    render(<Textarea description="More information" label="Test input" />)

    // Assert
    const input = screen.getByLabelText('Test input')
    const description = screen.getByText('More information')
    expect(input).toHaveAttribute('aria-describedby', description.id)
  })

  it('renders a required Textarea', async () => {
    // Act
    render(<Textarea label="My required text" value="" required={true}/>)

    // Assert
    const textarea = screen.getByLabelText('My required text*')
    expect(textarea).toHaveClass('is-invalid')
  })
})
