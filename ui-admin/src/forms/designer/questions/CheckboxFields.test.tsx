import { fireEvent, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { CheckboxQuestion } from '@juniper/ui-core'

import { CheckboxFields } from './CheckboxFields'

describe('CheckboxFields', () => {
  it('has option to show "None" option', () => {
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
    render(<CheckboxFields disabled={false} question={question} onChange={jest.fn()} />)

    // Assert
    const input = screen.getByLabelText('Show "None" option')
    expect((input as HTMLInputElement).checked).toBe(false)
  })

  it('sets default values for "None" option config', async () => {
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

    render(<CheckboxFields disabled={false} question={question} onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Show "None" option')
    await user.click(input)

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      showNoneItem: true,
      noneText: 'None of the above',
      noneValue: 'noneOfAbove'
    })
  })

  describe('when "None" option is shown', () => {
    const question: CheckboxQuestion = {
      name: 'test_question',
      title: 'Pick some',
      type: 'checkbox',
      choices: [
        { value: 'foo', text: 'foo' },
        { value: 'bar', text: 'bar' },
        { value: 'baz', text: 'baz' }
      ],
      showNoneItem: true,
      noneText: 'None of the above',
      noneValue: 'noneOfAbove'
    }

    it('shows "None" option text', () => {
      // Act
      render(<CheckboxFields disabled={false} question={question} onChange={jest.fn()} />)

      // Assert
      const input = screen.getByLabelText('Label')
      expect((input as HTMLInputElement).value).toBe('None of the above')
    })

    it('updates "None" option text', () => {
      // Arrange
      const onChange = jest.fn()
      render(<CheckboxFields disabled={false} question={question} onChange={onChange} />)

      // Act
      const input = screen.getByLabelText('Label')
      fireEvent.change(input, { target: { value: 'None of these' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith({
        ...question,
        noneText: 'None of these'
      })
    })

    it('shows "None" option value', () => {
      // Act
      render(<CheckboxFields disabled={false} question={question} onChange={jest.fn()} />)

      // Assert
      const input = screen.getByLabelText('Value')
      expect((input as HTMLInputElement).value).toBe('noneOfAbove')
    })

    it('updates "None" option value', () => {
      // Arrange
      const onChange = jest.fn()
      render(<CheckboxFields disabled={false} question={question} onChange={onChange} />)

      // Act
      const input = screen.getByLabelText('Value')
      fireEvent.change(input, { target: { value: 'noneOfThese' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith({
        ...question,
        noneValue: 'noneOfThese'
      })
    })
  })
})
