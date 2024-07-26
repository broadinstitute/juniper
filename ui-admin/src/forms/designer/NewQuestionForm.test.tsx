import React from 'react'
import { act, render, screen } from '@testing-library/react'
import { NewQuestionForm } from './NewQuestionForm'
import { userEvent } from '@testing-library/user-event'
import { MOCK_ENGLISH_LANGUAGE } from '../../test-utils/mocking-utils'

describe('NewQuestionForm', () => {
  test('renders the default view for a new question', () => {
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    screen.getByLabelText('Question stable ID')
    const questionTypeSelect = screen.getByLabelText('Question type')
    expect(questionTypeSelect).toHaveValue('Select a question type')
  })

  test('filters special characters in stableId', async () => {
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)
    await userEvent.type(screen.getByLabelText('Question stable ID'), 'blah d#_* blah1')
    expect(screen.getByLabelText('Question stable ID')).toHaveValue('blahd_blah1')
  })

  test('updates to the appropriate QuestionDesigner when a new question type is selected', async () => {
    const user = userEvent.setup()
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    const questionTypeSelect = screen.getByLabelText('Question type')
    await act(() => user.selectOptions(questionTypeSelect, 'checkbox'))

    expect(questionTypeSelect).toHaveValue('checkbox')
    expect(screen.getAllByText('Checkbox question')).not.toHaveLength(0)

    // now check we can change the type to html
    await act(() => user.selectOptions(questionTypeSelect, 'html'))

    expect(questionTypeSelect).toHaveValue('html')
    expect(screen.getAllByText('Html question')).not.toHaveLength(0)
    expect(screen.queryByLabelText('Question text')).not.toBeInTheDocument()
  })

  test('renders freetext input', async () => {
    //Arrange
    const user = userEvent.setup()
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

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
    render(<NewQuestionForm onCreate={() => jest.fn()} questionTemplates={[]} readOnly={false}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    //Act
    const questionTypeSelect = screen.getByLabelText('Question type')
    const freetextModeCheckbox = screen.getByLabelText('Enable freetext mode')
    await act(() => user.click(freetextModeCheckbox))

    const freetextInput = screen.getByLabelText('Freetext') as HTMLInputElement
    await act(() => user.type(freetextInput, 'This is a question!\nThis is an option!\nThis is another option!'))

    //Assert
    expect(questionTypeSelect).toHaveValue('radiogroup')
    expect(screen.getAllByText('Radio group', { exact: false })).not.toHaveLength(0)
  })

  test('renders the question template picker when there are templates', async () => {
    //Arrange
    render(<NewQuestionForm
      onCreate={() => jest.fn()}
      questionTemplates={[{ name: 'oh_oh_template', title: 'A template', type: 'text' }]}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}
      readOnly={false}/>
    )

    //Assert
    expect(screen.getByText('Question template (optional)')).toBeInTheDocument()
  })

  test('does not render the question template picker where aren\'t any templates', async () => {
    //Arrange
    render(<NewQuestionForm
      onCreate={() => jest.fn()}
      questionTemplates={[]} currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}
      readOnly={false}/>
    )

    //Assert
    expect(screen.queryByText('Question template (optional)')).not.toBeInTheDocument()
  })
})
