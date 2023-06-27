import { fireEvent, render, screen } from '@testing-library/react'
import React from 'react'

import { NumberInput } from './NumberInput'

describe('NumberInput', () => {
  it('renders a number input with label', () => {
    // Act
    render(<NumberInput label="Value" />)

    // Assert
    const input = screen.getByLabelText('Value')
    expect(input).toBeInstanceOf(HTMLInputElement)
    expect(input).toHaveAttribute('type', 'number')
  })

  it('calls onChange with input value', () => {
    // Arrange
    const onChange = jest.fn()
    render(<NumberInput label="Test input" value={1} onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Test input')
    fireEvent.change(input, { target: { value: '2' } })

    // Assert
    expect(onChange).toHaveBeenCalledWith(2)
  })
})
