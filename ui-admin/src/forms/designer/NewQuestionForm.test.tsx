import React from 'react'
import { act, render, screen } from '@testing-library/react'
import { NewQuestionForm } from './NewQuestionForm'
import { questionTypeDescriptions } from './questions/questionTypes'
import userEvent from '@testing-library/user-event'

describe('NewQuestionForm', () => {
  test('renders the default view for a new question', () => {
    //Arrange
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false} />)

    //Assert
    screen.getByLabelText('Question stable ID')
    const questionTypeSelect = screen.getByLabelText('Question type')
    expect(questionTypeSelect).toHaveValue('Select a question type')
  })

  test('updates to the appropriate QuestionDesigner when a new question type is selected', async () => {
    //Arrange
    const user = userEvent.setup()
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}/>)

    //Act
    const questionTypeSelect = screen.getByLabelText('Question type')
    await act(() => user.selectOptions(questionTypeSelect, 'checkbox'))

    //Assert
    expect(questionTypeSelect).toHaveValue('checkbox')
    expect(screen.getByText(questionTypeDescriptions.checkbox)).toBeInTheDocument()
  })

  test('renders freetext input', async () => {
    //Arrange
    const user = userEvent.setup()
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}/>)

    //Act
    const questionTypeSelect = screen.getByLabelText('Question type')
    const freetextModeCheckbox = screen.getByLabelText('Enable freetext mode')
    await act(() => user.click(freetextModeCheckbox))

    //Assert
    screen.getByLabelText('Freetext')
    expect(questionTypeSelect).toHaveValue('Select a question type')
  })

  test('updates to the appropriate QuestionDesigner based on freetext input', async () => {
    //Arrange
    const user = userEvent.setup()
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}/>)

    //Act
    const questionTypeSelect = screen.getByLabelText('Question type')
    const freetextModeCheckbox = screen.getByLabelText('Enable freetext mode')
    await act(() => user.click(freetextModeCheckbox))

    const freetextInput = screen.getByLabelText('Freetext') as HTMLInputElement
    await act(() => user.type(freetextInput, 'This is a question!\nThis is an option!\nThis is another option!'))

    //Assert
    expect(questionTypeSelect).toHaveValue('radiogroup')
    expect(screen.getByText(questionTypeDescriptions.radiogroup)).toBeInTheDocument()
  })
})
