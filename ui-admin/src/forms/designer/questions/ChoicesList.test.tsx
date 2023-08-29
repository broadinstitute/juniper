import { act, fireEvent, getByLabelText, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

import { CheckboxQuestion } from '@juniper/ui-core'

import { ChoicesList } from './ChoicesList'

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
    // Act
    render(<ChoicesList question={question} readOnly={false} onChange={jest.fn()} />)

    // Assert
    const choiceListItems = screen.getAllByRole('listitem')

    ;['Foo', 'Bar', 'Baz'].forEach((label, index) => {
      const labelInput = getByLabelText(choiceListItems[index], 'Text')
      expect((labelInput as HTMLInputElement).value).toBe(label)

      const valueInput = getByLabelText(choiceListItems[index], 'Value')
      expect((valueInput as HTMLInputElement).value).toBe(label.toLowerCase())
    })
  })

  it('allows changing choice labels', () => {
    // Arrange
    const onChange = jest.fn()
    render(<ChoicesList question={question} readOnly={false} onChange={onChange} />)

    const barChoice = screen.getAllByRole('listitem')[1]

    // Act
    const barLabelInput = getByLabelText(barChoice, 'Text')
    fireEvent.change(barLabelInput, { target: { value: 'BAR' } })

    // Assert
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
    render(<ChoicesList question={question} readOnly={false} onChange={onChange} />)

    const barChoice = screen.getAllByRole('listitem')[1]

    // Act
    const barLabelInput = getByLabelText(barChoice, 'Value')
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

  it('automatically generates values based on text', () => {
    // Arrange
    const onChange = jest.fn()
    render(<ChoicesList question={question} readOnly={false} onChange={onChange} />)

    const barChoice = screen.getAllByRole('listitem')[1]

    // Act
    const barLabelInput = getByLabelText(barChoice, 'Text')
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
    render(<ChoicesList question={question} readOnly={false} onChange={onChange} />)

    const barChoice = screen.getAllByRole('listitem')[1]
    const moveUpButton = getByLabelText(barChoice, 'Move this choice before the previous one')
    const moveDownButton = getByLabelText(barChoice, 'Move this choice after the next one')

    // Act
    await act(() => user.click(moveUpButton))

    // Assert
    expect(onChange).toBeCalledWith({
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
    expect(onChange).toBeCalledWith({
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
    render(<ChoicesList question={question} readOnly={false} onChange={onChange} />)

    const barChoice = screen.getAllByRole('listitem')[1]
    const deleteButton = getByLabelText(barChoice, 'Delete this choice')

    // Act
    await act(() => user.click(deleteButton))

    // Assert
    expect(onChange).toBeCalledWith({
      ...question,
      choices: [
        { value: 'foo', text: 'Foo' },
        { value: 'baz', text: 'Baz' }
      ]
    })
  })
})
