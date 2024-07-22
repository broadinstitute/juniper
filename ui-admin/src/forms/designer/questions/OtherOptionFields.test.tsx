import { fireEvent, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { CheckboxQuestion } from '@juniper/ui-core'

import { OtherOptionFields } from './OtherOptionFields'

describe('OtherOptionFields', () => {
  it('has option to show "Other" option', () => {
    // Arrange
    const question: CheckboxQuestion = {
      name: 'test_question',
      title: 'Pick some',
      type: 'checkbox',
      choices: [
        { value: 'foo', text: 'foo' },
        { value: 'bar', text: 'bar' },
        { value: 'baz', text: 'baz' }
      ]
    }

    // Act
    render(<OtherOptionFields disabled={false} question={question} onChange={jest.fn()} />)

    // Assert
    const input = screen.getByLabelText('Show "Other" option')
    expect((input as HTMLInputElement).checked).toBe(false)
  })

  it('sets default values for "Other" option config', async () => {
    // Arrange
    const user = userEvent.setup()

    const question: CheckboxQuestion = {
      name: 'test_question',
      title: 'Pick some',
      type: 'checkbox',
      choices: [
        { value: 'foo', text: 'foo' },
        { value: 'bar', text: 'bar' },
        { value: 'baz', text: 'baz' }
      ]
    }

    const onChange = jest.fn()

    render(<OtherOptionFields disabled={false} question={question} onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Show "Other" option')
    await user.click(input)

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      showOtherItem: true,
      otherText: 'Other',
      otherPlaceholder: 'Please specify',
      otherErrorText: 'A description is required for choices of "other".'
    })
  })

  describe('when "Other" option is shown', () => {
    const question: CheckboxQuestion = {
      name: 'test_question',
      title: 'Pick some',
      type: 'checkbox',
      choices: [
        { value: 'foo', text: 'foo' },
        { value: 'bar', text: 'bar' },
        { value: 'baz', text: 'baz' }
      ],
      showOtherItem: true,
      otherText: 'Other',
      otherPlaceholder: 'Please specify',
      otherErrorText: 'A description is required for choices of "other".'
    }

    it('shows "Other" option text', () => {
      // Act
      render(<OtherOptionFields disabled={false} question={question} onChange={jest.fn()} />)

      // Assert
      const input = screen.getByLabelText('Label')
      expect((input as HTMLInputElement).value).toBe('Other')
    })

    it('updates "Other" option text', () => {
      // Arrange
      const onChange = jest.fn()
      render(<OtherOptionFields disabled={false} question={question} onChange={onChange} />)

      // Act
      const input = screen.getByLabelText('Label')
      fireEvent.change(input, { target: { value: 'Something else' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith({
        ...question,
        otherText: 'Something else'
      })
    })

    it('shows "Other" description placeholder', () => {
      // Act
      render(<OtherOptionFields disabled={false} question={question} onChange={jest.fn()} />)

      // Assert
      const input = screen.getByLabelText('Placeholder')
      expect((input as HTMLInputElement).value).toBe('Please specify')
    })

    it('updates "Other" description placeholder', () => {
      // Arrange
      const onChange = jest.fn()
      render(<OtherOptionFields disabled={false} question={question} onChange={onChange} />)

      // Act
      const input = screen.getByLabelText('Placeholder')
      fireEvent.change(input, { target: { value: 'Please describe' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith({
        ...question,
        otherPlaceholder: 'Please describe'
      })
    })

    it('shows "Other" description error message', () => {
      // Act
      render(<OtherOptionFields disabled={false} question={question} onChange={jest.fn()} />)

      // Assert
      const input = screen.getByLabelText('Error message')
      expect((input as HTMLInputElement).value).toBe('A description is required for choices of "other".')
    })

    it('updates "Other" description error message', () => {
      // Arrange
      const onChange = jest.fn()
      render(<OtherOptionFields disabled={false} question={question} onChange={onChange} />)

      // Act
      const input = screen.getByLabelText('Error message')
      fireEvent.change(input, { target: { value: 'A description is required' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith({
        ...question,
        otherErrorText: 'A description is required'
      })
    })
  })
})
