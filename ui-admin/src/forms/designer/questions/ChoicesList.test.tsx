import { act, fireEvent, getByLabelText, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { CheckboxQuestion, QuestionChoice } from '@juniper/ui-core'

import { ChoicesList } from './ChoicesList'
import { MOCK_ENGLISH_LANGUAGE } from '../../../test-utils/mocking-utils'

describe('ChoicesList', () => {
  const question: CheckboxQuestion = {
    name: 'test_question',
    title: 'Pick some',
    type: 'checkbox',
    choices: [
      { value: 'foo', text: 'Foo' },
      { value: 'bar', text: 'Bar' },
      { value: 'baz', text: 'Baz' }
    ]
  }

  it('renders list of choices with inputs for text and value', () => {
    render(<ChoicesList question={question} isNewQuestion={false} readOnly={false} onChange={jest.fn()}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    // for each table row (choice), confirm the text and stableId inputs are rendered
    const tableRows = screen.getAllByRole('row')
    ;['Foo', 'Bar', 'Baz'].forEach((label, index) => {
      const labelInput = getByLabelText(tableRows[index + 1], 'text')
      expect((labelInput as HTMLInputElement).value).toBe(label)

      const valueInput = getByLabelText(tableRows[index + 1], 'value')
      expect((valueInput as HTMLInputElement).value).toBe(label.toLowerCase())
    })
  })

  it('allows changing choice labels', () => {
    const onChange = jest.fn()
    render(<ChoicesList question={question} isNewQuestion={false} readOnly={false} onChange={onChange}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    const barChoice = screen.getAllByRole('row')[2]

    const barLabelInput = getByLabelText(barChoice, 'text')
    fireEvent.change(barLabelInput, { target: { value: 'BAR' } })

    expect(onChange).toHaveBeenCalledWith({
      ...question,
      choices: [
        { value: 'foo', text: 'Foo' },
        { value: 'bar', text: 'BAR' },
        { value: 'baz', text: 'Baz' }
      ]
    })
  })

  it('allows changing choice values', () => {
    // Arrange
    const onChange = jest.fn()
    render(<ChoicesList question={question} isNewQuestion={false} readOnly={false} onChange={onChange}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    const barChoice = screen.getAllByRole('row')[2]

    // Act
    const barLabelInput = getByLabelText(barChoice, 'value')
    fireEvent.change(barLabelInput, { target: { value: 'BAR' } })

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      choices: [
        { value: 'foo', text: 'Foo' },
        { value: 'BAR', text: 'Bar' },
        { value: 'baz', text: 'Baz' }
      ]
    })
  })

  it('automatically generates values based on text if question is new', () => {
    // Arrange
    const onChange = jest.fn()
    render(<ChoicesList question={question} isNewQuestion={true} readOnly={false} onChange={onChange}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    const barChoice = screen.getAllByRole('row')[2]

    // Act
    const barLabelInput = getByLabelText(barChoice, 'text')
    fireEvent.change(barLabelInput, { target: { value: 'This is a test question' } })

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      choices: [
        { value: 'foo', text: 'Foo' },
        { value: 'thisIsATestQuestion', text: 'This is a test question' },
        { value: 'baz', text: 'Baz' }
      ]
    })
  })

  it('allows reordering choices', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<ChoicesList question={question} isNewQuestion={false} readOnly={false} onChange={onChange}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    const barChoice = screen.getAllByRole('row')[2]
    const moveUpButton = getByLabelText(barChoice, 'Move this choice before the previous one')
    const moveDownButton = getByLabelText(barChoice, 'Move this choice after the next one')

    // Act
    await act(() => user.click(moveUpButton))

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      choices: [
        { value: 'bar', text: 'Bar' },
        { value: 'foo', text: 'Foo' },
        { value: 'baz', text: 'Baz' }
      ]
    })

    // Act
    await act(() => user.click(moveDownButton))

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      choices: [
        { value: 'foo', text: 'Foo' },
        { value: 'baz', text: 'Baz' },
        { value: 'bar', text: 'Bar' }
      ]
    })
  })

  it('allows deleting choices', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<ChoicesList question={question} isNewQuestion={false} readOnly={false} onChange={onChange}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    const barChoice = screen.getAllByRole('row')[2]
    const deleteButton = getByLabelText(barChoice, 'Delete this choice')

    // Act
    await act(() => user.click(deleteButton))

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...question,
      choices: [
        { value: 'foo', text: 'Foo' },
        { value: 'baz', text: 'Baz' }
      ]
    })
  })

  it('renders nothing for questions with no choices', async () => {
    render(<ChoicesList question={{
      ...question,
      choices: undefined as unknown as QuestionChoice[]
    }} isNewQuestion={false} readOnly={false} onChange={jest.fn}
    currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    expect(screen.queryByText('Choices')).not.toBeInTheDocument()
  })
})
