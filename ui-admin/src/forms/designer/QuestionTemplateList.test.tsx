import { act, getByRole, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { QuestionTemplateList } from './QuestionTemplateList'

describe('QuestionTemplateList', () => {
  const formContent: FormContent = {
    title: 'Test form',
    pages: [
      {
        elements: [
          {
            name: 'question1',
            questionTemplateName: 'templateA'
          }
        ]
      }
    ],
    questionTemplates: [
      {
        name: 'templateA',
        type: 'text',
        title: 'A?'
      },
      {
        name: 'templateB',
        type: 'text',
        title: 'B?'
      }
    ]
  }

  it('renders list of question templates in form', () => {
    // Act
    render(<QuestionTemplateList formContent={formContent} readOnly={false} onChange={jest.fn()} />)

    // Assert
    const listItems = screen.getAllByRole('listitem')
    expect(listItems.map(el => el.textContent)).toEqual(['templateA', 'templateB'])
  })

  it('allows deleting question templates that are not referenced by a question', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<QuestionTemplateList formContent={formContent} readOnly={false} onChange={onChange} />)

    // Act
    const listItems = screen.getAllByRole('listitem')
    await act(() => user.click(getByRole(listItems[1], 'button')))

    // Assert
    expect(onChange).toHaveBeenCalledWith({
      ...formContent,
      questionTemplates: [
        {
          name: 'templateA',
          type: 'text',
          title: 'A?'
        }
      ]
    })
  })

  it('does not allow deleting question templates that are referenced by a question', async () => {
    // Arrange
    const user = userEvent.setup()

    const onChange = jest.fn()
    render(<QuestionTemplateList formContent={formContent} readOnly={false} onChange={onChange} />)

    // Act
    const listItems = screen.getAllByRole('listitem')
    const deleteTemplateAButton = getByRole(listItems[0], 'button')
    await act(() => user.click(deleteTemplateAButton))

    // Assert
    expect(deleteTemplateAButton).toHaveAttribute('aria-disabled', 'true')
    expect(onChange).not.toHaveBeenCalled()
  })
})
