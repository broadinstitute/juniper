import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { NewQuestionForm } from './NewQuestionForm'
import { questionTypeDescriptions } from './questions/questionTypes'
import userEvent from '@testing-library/user-event'

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

  test('renders freetext input', () => {
    //Arrange
    render(<NewQuestionForm onCreate={() => jest.fn()} readOnly={false} />)

    //Act
    const questionTypeSelect = screen.getByLabelText('Question type')
    fireEvent.change(questionTypeSelect, { target: { value: 'freetext' } })

    //Assert
    const freetextInput = screen.getByLabelText('Freetext')
    expect((questionTypeSelect as HTMLSelectElement).value).toBe('freetext')
    expect(freetextInput as HTMLInputElement).toBeInTheDocument()
    expect(screen.getByText(questionTypeDescriptions.text)).toBeInTheDocument() //initial question type is text
  })

  test('updates to the appropriate QuestionDesigner based on freetext input', async () => {
    //Arrange
    render(<NewQuestionForm onCreate={() => jest.fn()} readOnly={false}/>)

    //Act
    const questionTypeSelect = screen.getByLabelText('Question type')
    fireEvent.change(questionTypeSelect, {target: {value: 'freetext'}})
    const freetextInput = screen.getByLabelText('Freetext') as HTMLInputElement
    userEvent.pointer({target: freetextInput, offset: 0, keys: '[MouseLeft]'})
    userEvent.keyboard('This is a question!\nThis is an option!\nThis is another option!')

    //Assert
    await waitFor(() => expect((questionTypeSelect as HTMLSelectElement).value).toBe('radiogroup'))
    await waitFor(() => expect(screen.getByText(questionTypeDescriptions.radiogroup)).toBeInTheDocument())
  })
})
