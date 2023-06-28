import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

import { Question } from '@juniper/ui-core'

import { BaseFields } from './BaseFields'

describe('BaseFields', () => {
  const question: Question = {
    name: 'test_question',
    title: 'What?',
    description: 'This is a test question',
    isRequired: true,
    type: 'text'
  }

  it('shows question title', () => {
    // Act
    render(<BaseFields disabled={false} question={question} onChange={jest.fn()} />)

    // Assert
    const input = screen.getByLabelText('Question text')
    expect((input as HTMLInputElement).value).toBe('What?')
  })

  it('updates question title', () => {
    // Arrange
    const onChange = jest.fn()
    render(<BaseFields disabled={false} question={question} onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Question text')
    fireEvent.change(input, { target: { value: 'Why?' } })

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      title: 'Why?'
    })
  })

  it('shows question description', () => {
    // Act
    render(<BaseFields disabled={false} question={question} onChange={jest.fn()} />)

    // Assert
    const input = screen.getByLabelText('Description')
    expect((input as HTMLInputElement).value).toBe('This is a test question')
  })

  it('updates question description', () => {
    // Arrange
    const onChange = jest.fn()
    render(<BaseFields disabled={false} question={question} onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Description')
    fireEvent.change(input, { target: { value: 'More information' } })

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      description: 'More information'
    })
  })

  it('shows required flag', () => {
    // Act
    render(<BaseFields disabled={false} question={question} onChange={jest.fn()} />)

    // Assert
    const input = screen.getByLabelText('Require response')
    expect((input as HTMLInputElement).checked).toBe(true)
  })

  it('updates required flag', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<BaseFields disabled={false} question={question} onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Require response')
    await user.click(input)

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      isRequired: false
    })
  })
})
