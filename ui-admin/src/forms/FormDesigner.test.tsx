import React from 'react'
import { FormContent } from '@juniper/ui-core'
import { getByLabelText, render, screen, waitFor } from '@testing-library/react'
import { FormDesigner } from './FormDesigner'
import { renderWithRouter, setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'
import { baseQuestions } from './designer/questions/questionTypes'

const formContent: FormContent = {
  title: 'Test survey',
  pages: [
    {
      elements: [
        {
          name: 'test_firstName',
          type: 'text',
          title: 'First name',
          isRequired: true
        },
        {
          name: 'test_lastName',
          type: 'text',
          title: 'Last name',
          isRequired: true
        }
      ]
    }
  ]
}

describe('FormDesigner', () => {
  it('renders form', () => {
    renderWithRouter(<FormDesigner content={formContent} onChange={jest.fn()}/>)

    expect(screen.getByLabelText('test_firstName')).toBeInTheDocument()
    expect(screen.getByLabelText('test_lastName')).toBeInTheDocument()
    // 'pages' should appear in table of contents, and be currently selected
    expect(screen.getAllByText('Pages')).toHaveLength(2)
  })

  it('shows elements based on the path', () => {
    renderWithRouter(<FormDesigner content={formContent} onChange={jest.fn()}/>,
      ['/forms/surveys/oh_oh_basicInfo?selectedElementPath=pages[0].elements[1]'])
    // we should be showing the editor for the second question
    expect(screen.getByText('Last name')).toBeInTheDocument()
    expect(screen.getByText('Question text')).toBeInTheDocument()
    expect(screen.queryByText('First name')).not.toBeInTheDocument()
  })

  it('adds questions after a current question', async () => {
    const updateValue = jest.fn()
    const { RoutedComponent, router } = setupRouterTest(<FormDesigner content={formContent} onChange={updateValue}/>,
      ['/forms/surveys/oh_oh_basicInfo?selectedElementPath=pages[0].elements[1]'])
    render(RoutedComponent)
    // we should be showing the editor for the second question
    await userEvent.click(screen.getByText('Add next question'))
    expect(screen.getByText('New Question')).toBeInTheDocument()
    const newQuestionForm = screen.getByTestId('newQuestionForm')
    await userEvent.type(getByLabelText(newQuestionForm, 'Question stable ID'), 'stableId1')
    await userEvent.selectOptions(getByLabelText(newQuestionForm, 'Question type'), 'text')
    await userEvent.type(getByLabelText(newQuestionForm, 'Question text*'), 'fave color?')
    await userEvent.click(screen.getByText('Create question'))
    await waitFor(() => expect(router.state.location.search)
      .toContain('selectedElementPath=pages%5B0%5D.elements%5B2%5D'))
    expect(updateValue).toHaveBeenCalledWith({
      ...formContent,
      pages: [
        {
          elements: [
            ...formContent.pages[0].elements,
            {
              ...baseQuestions.text,
              name: 'stableId1',
              title: 'fave color?'
            }
          ]
        }
      ]
    })
  })

  it('adds questions to the end of a page', async () => {
    const updateValue = jest.fn()
    const { RoutedComponent, router } = setupRouterTest(<FormDesigner content={formContent} onChange={updateValue}/>,
      ['/forms/surveys/oh_oh_basicInfo?selectedElementPath=pages[0]'])
    render(RoutedComponent)
    // we should be showing the page 1 summary
    expect(screen.getAllByText('Page 1')).toHaveLength(2)

    await userEvent.click(screen.getByText('Add question'))
    expect(screen.getByText('New Question')).toBeInTheDocument()
    const newQuestionForm = screen.getByTestId('newQuestionForm')
    await userEvent.type(getByLabelText(newQuestionForm, 'Question stable ID'), 'stableId1')
    await userEvent.selectOptions(getByLabelText(newQuestionForm, 'Question type'), 'dropdown')
    await userEvent.type(getByLabelText(newQuestionForm, 'Question text*'), 'fave color?')
    await userEvent.click(screen.getByText('Create question'))
    await waitFor(() => expect(router.state.location.search)
      .toContain('selectedElementPath=pages%5B0%5D.elements%5B2%5D'))
    expect(updateValue).toHaveBeenCalledWith({
      ...formContent,
      pages: [
        {
          elements: [
            ...formContent.pages[0].elements,
            {
              ...baseQuestions.dropdown,
              name: 'stableId1',
              title: 'fave color?'
            }
          ]
        }
      ]
    })
  })
})
