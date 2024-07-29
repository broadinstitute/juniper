import React from 'react'
import { FormContent, renderWithRouter, setupRouterTest } from '@juniper/ui-core'
import { getByLabelText, render, screen, waitFor } from '@testing-library/react'
import { FormDesigner } from './FormDesigner'
import { userEvent } from '@testing-library/user-event'
import { baseQuestions } from './designer/questions/questionTypes'
import { MOCK_ENGLISH_LANGUAGE } from '../test-utils/mocking-utils'

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
    renderWithRouter(<FormDesigner content={formContent} onChange={jest.fn()}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>)

    expect(screen.getByLabelText('test_firstName')).toBeInTheDocument()
    expect(screen.getByLabelText('test_lastName')).toBeInTheDocument()
    // 'pages' should appear in table of contents, and be currently selected
    expect(screen.getAllByText('Pages')).toHaveLength(2)
  })

  it('shows elements based on the path', async () => {
    renderWithRouter(<FormDesigner content={formContent} onChange={jest.fn()}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>,
    ['/forms/surveys/oh_oh_basicInfo?selectedElementPath=pages[0].elements[1]'])
    // we should be showing the editor for the second question
    expect(screen.getByText('Last name')).toBeInTheDocument()
    expect(screen.queryAllByText('Text')).toHaveLength(2)
    expect(screen.queryByText('First name')).not.toBeInTheDocument()
    await userEvent.click(screen.getByText('test_firstName'))
    expect(screen.queryByText('First name')).toBeInTheDocument()
  })

  it('adds questions after a current question', async () => {
    // dummy update function that just invokes the callback
    const updateValue = jest.fn()
      .mockImplementation((content: FormContent, callback?: () => void) => { callback?.() })
    const { RoutedComponent, router } = setupRouterTest(
      <FormDesigner content={formContent} onChange={updateValue}
        currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]}/>,
      ['/forms/surveys/oh_oh_basicInfo?selectedElementPath=pages[0].elements[1]'])
    render(RoutedComponent)
    // we should be showing the editor for the second question
    await userEvent.click(screen.getByText('Add next question'))
    expect(screen.getByText('New Question')).toBeInTheDocument()
    const newQuestionForm = screen.getByTestId('newQuestionForm')
    await userEvent.type(getByLabelText(newQuestionForm, 'Question stable ID'), 'stableId1')
    await userEvent.selectOptions(getByLabelText(newQuestionForm, 'Question type'), 'text')
    await userEvent.type(getByLabelText(newQuestionForm, 'Text*'), 'fave color?')
    await userEvent.click(screen.getByText('Create question'))
    await waitFor(() => expect(router.state.location.search)
      .toContain('?selectedElementPath=pages%5B0%5D.elements%5B2%5D'))
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
    }, expect.anything())
  })

  it('adds questions to the end of a page', async () => {
    // dummy update function that just invokes the callback
    const updateValue = jest.fn()
      .mockImplementation((content: FormContent, callback?: () => void) => { callback?.() })
    const { RoutedComponent, router } = setupRouterTest(<FormDesigner content={formContent}
      currentLanguage={MOCK_ENGLISH_LANGUAGE} supportedLanguages={[]} onChange={updateValue}/>,
    ['/forms/surveys/oh_oh_basicInfo?selectedElementPath=pages[0]'])
    render(RoutedComponent)
    // we should be showing the page 1 summary
    expect(screen.getAllByText('Page 1')).toHaveLength(2)

    await userEvent.click(screen.getByText('Add question'))
    expect(screen.getByText('New Question')).toBeInTheDocument()
    const newQuestionForm = screen.getByTestId('newQuestionForm')
    await userEvent.type(getByLabelText(newQuestionForm, 'Question stable ID'), 'stableId1')
    await userEvent.selectOptions(getByLabelText(newQuestionForm, 'Question type'), 'dropdown')
    await userEvent.type(getByLabelText(newQuestionForm, 'Text*'), 'fave color?')
    await userEvent.click(screen.getByText('Create question'))
    await waitFor(() => expect(router.state.location.search)
      .toContain('?selectedElementPath=pages%5B0%5D.elements%5B2%5D'))
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
    }, expect.anything())
  })
})
