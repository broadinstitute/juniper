import { fireEvent, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { Question } from '@juniper/ui-core'

import { VisibilityFields } from './VisibilityFields'

describe('VisibilityFields', () => {
  it('has option to conditionally show question', () => {
    // Arrange
    const question: Question = {
      name: 'test_question',
      title: 'What?',
      type: 'text'
    }

    // Act
    render(<VisibilityFields disabled={false} question={question} onChange={jest.fn()} />)

    // Assert
    const input = screen.getByLabelText('Conditionally show this question')
    expect((input as HTMLInputElement).checked).toBe(false)
  })

  it('adds visibleIf field when checked', async () => {
    // Arrange
    const user = userEvent.setup()

    const question: Question = {
      name: 'test_question',
      title: 'What?',
      type: 'text'
    }

    const onChange = jest.fn()

    render(<VisibilityFields disabled={false} question={question} onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Conditionally show this question')
    await user.click(input)

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      visibleIf: ''
    })
  })

  it('removes visibleIf field when unchecked', async () => {
    // Arrange
    const user = userEvent.setup()

    const question: Question = {
      name: 'test_question',
      title: 'What?',
      type: 'text',
      visibleIf: '{other_question} = "Yes"'
    }

    const onChange = jest.fn()

    render(<VisibilityFields disabled={false} question={question} onChange={onChange} />)

    // Act
    const input = screen.getByLabelText('Conditionally show this question')
    await user.click(input)

    // Assert
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { visibleIf, ...otherQuestionFields } = question
    expect(onChange).toHaveBeenCalledWith(otherQuestionFields)
  })

  describe('when "Conditionally show this question" option is selected', () => {
    const question: Question = {
      name: 'test_question',
      title: 'What?',
      type: 'text',
      visibleIf: '{other_question} = "Yes"'
    }

    it('shows visibility express', () => {
      // Act
      render(<VisibilityFields disabled={false} question={question} onChange={jest.fn()} />)

      // Assert
      const input = screen.getByLabelText('Visibility expression*')
      expect((input as HTMLInputElement).value).toBe('{other_question} = "Yes"')
    })

    it('updates visibility expression', () => {
      // Arrange
      const onChange = jest.fn()
      render(<VisibilityFields disabled={false} question={question} onChange={onChange} />)

      // Act
      const input = screen.getByLabelText('Visibility expression*')
      fireEvent.change(input, { target: { value: 'true' } })

      // Assert
      expect(onChange).toHaveBeenCalledWith({
        ...question,
        visibleIf: 'true'
      })
    })

    it('displays InfoPopups describing conditional visibility', async () => {
      const user = userEvent.setup()
      render(<VisibilityFields disabled={false} question={question} onChange={jest.fn()} />)

      await user.click(screen.getByLabelText('info popup'))
      expect(await screen.findByText('Conditional Visibility documentation')).toBeTruthy()
    })
  })
})
