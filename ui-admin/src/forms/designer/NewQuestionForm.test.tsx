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
    const user = userEvent.setup()
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}/>)

    const questionTypeSelect = screen.getByLabelText('Question type')
    await act(() => user.selectOptions(questionTypeSelect, 'checkbox'))

    expect(questionTypeSelect).toHaveValue('checkbox')
    expect(screen.getByText(questionTypeDescriptions.checkbox)).toBeInTheDocument()

    // now check we can change the type to html
    await act(() => user.selectOptions(questionTypeSelect, 'html'))

    expect(questionTypeSelect).toHaveValue('html')
    expect(screen.getByText(questionTypeDescriptions.html)).toBeInTheDocument()
    expect(screen.queryByLabelText('Question text')).not.toBeInTheDocument()
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

  test('renders the question template picker when there are templates', async () => {
    //Arrange
    render(<NewQuestionForm
      onCreate={() => jest.fn()}
      questionTemplates={[{ name: 'oh_oh_template', title: 'A template', type: 'text' }]}
      readOnly={false}/>
    )

    //Assert
    expect(screen.getByText('Question template (optional)')).toBeInTheDocument()
  })

  test('does not render the question template picker where aren\'t any templates', async () => {
    //Arrange
    render(<NewQuestionForm
      onCreate={() => jest.fn()}
      questionTemplates={[]}
      readOnly={false}/>
    )

    //Assert
    expect(screen.queryByText('Question template (optional)')).not.toBeInTheDocument()
  })
})
