import { unset } from 'lodash/fp'
import { fireEvent, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { TextQuestion } from '@juniper/ui-core'

import { TextFields } from './TextFields'

describe('TextFields', () => {
  it('has menu to choose input type', () => {
    // Arrange
    const question: TextQuestion = {
      name: 'test_question',
      title: 'What?',
      type: 'text'
    }

    // Act
    render(<TextFields disabled={false} question={question} onChange={jest.fn()} />)

    // Assert
    const inputTypeMenu = screen.getByLabelText('Input type')
    expect((inputTypeMenu as HTMLSelectElement).value).toBe('text')
  })

  it('sets input type', async () => {
    // Arrange
    const user = userEvent.setup()

    const question: TextQuestion = {
      name: 'test_question',
      title: 'What?',
      type: 'text'
    }

    const onChange = jest.fn()
    render(<TextFields disabled={false} question={question} onChange={onChange} />)

    // Act
    const inputTypeMenu = screen.getByLabelText('Input type')
    await user.selectOptions(inputTypeMenu, 'number')

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      inputType: 'number'
    })
  })

  describe('number input type', () => {
    const question: TextQuestion = {
      name: 'test_question',
      title: 'How mamy?',
      type: 'text',
      inputType: 'number',
      min: 0,
      max: 10
    }

    it('has inputs for min and max values', () => {
      // Arrange
      const onChange = jest.fn()

      // Act
      render(<TextFields disabled={false} question={question} onChange={onChange} />)
      const minInput = screen.getByLabelText('Minimum')
      const maxInput = screen.getByLabelText('Maximum')

      // Assert
      expect((minInput as HTMLInputElement).value).toBe('0')
      expect((maxInput as HTMLInputElement).value).toBe('10')

      // Act
      fireEvent.change(minInput, { target: { value: '2' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith({ ...question, min: 2 })

      // Act
      fireEvent.change(maxInput, { target: { value: '50' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith({ ...question, max: 50 })
    })

    it('renders undefined min and max values', () => {
      // Arrange
      const question: TextQuestion = {
        name: 'test_question',
        title: 'How mamy?',
        type: 'text',
        inputType: 'number'
      }

      // Act
      render(<TextFields disabled={false} question={question} onChange={jest.fn()} />)

      // Assert
      const minInput = screen.getByLabelText('Minimum')
      const maxInput = screen.getByLabelText('Maximum')
      expect((minInput as HTMLInputElement).value).toBe('')
      expect((maxInput as HTMLInputElement).value).toBe('')
    })

    it('sets undefined min and max values', () => {
      // Arrange
      const question: TextQuestion = {
        name: 'test_question',
        title: 'How mamy?',
        type: 'text',
        inputType: 'number',
        min: 0,
        max: 10
      }

      const onChange = jest.fn()
      render(<TextFields disabled={false} question={question} onChange={onChange} />)
      const minInput = screen.getByLabelText('Minimum')
      const maxInput = screen.getByLabelText('Maximum')

      // Act
      fireEvent.change(minInput, { target: { value: '' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith(unset('min', question))

      // Act
      fireEvent.change(maxInput, { target: { value: '' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith(unset('max', question))
    })
  })
})
