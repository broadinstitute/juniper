import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { NewQuestionForm } from './NewQuestionForm'
import { questionTypeDescriptions } from './questions/questionTypes'

describe('NewQuestionForm', () => {
  test('renders the default view for a new question', () => {
    //Arrange
    render(<NewQuestionForm onCreate={() => jest.fn()} readOnly={false} />)

    //Assert
    const questionStableIdInput = screen.getByLabelText('Question stable ID')
    const questionTypeSelect = screen.getByLabelText('Question type')
    expect(questionStableIdInput).toBeInTheDocument()
    expect(questionTypeSelect).toBeInTheDocument()
    expect((questionTypeSelect as HTMLSelectElement).value).toBe('placeholder')
  })

  test('updates to the appropriate QuestionDesigner when a new question type is selected', () => {
    //Arrange
    render(<NewQuestionForm onCreate={() => jest.fn()} readOnly={false} />)

    //Act
    const questionTypeSelect = screen.getByLabelText('Question type')
    fireEvent.change(questionTypeSelect, { target: { value: 'checkbox' } })

    //Assert
    expect((questionTypeSelect as HTMLSelectElement).value).toBe('checkbox')
    expect(screen.getByText(questionTypeDescriptions.checkbox)).toBeInTheDocument()
  })
})
