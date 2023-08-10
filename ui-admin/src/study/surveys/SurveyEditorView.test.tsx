import React from 'react'
import { render, screen } from '@testing-library/react'
import SurveyEditorView from './SurveyEditorView'

describe('SurveyEditorView', () => {

  test('shows the user a LoadedLocalDraftModal when a draft is loaded', async () => {
    //Arrange
    const FORM_DRAFT_KEY = 'surveyDraft_testForm_12'
    localStorage.setItem(FORM_DRAFT_KEY, JSON.stringify({}))

    jest.spyOn(Storage.prototype, 'getItem')

    render(<SurveyEditorView
      currentForm={{
        id: 'testForm',
        version: 12,
        content: '{}',
        stableId: 'testStableId',
        name: '',
        createdAt: 0,
        lastUpdatedAt: 0
      }}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)

    //Assert
    screen.getByText('Survey Draft Loaded')
  })

  test('checks local storage for a draft', async () => {
    //Arrange
    const FORM_DRAFT_KEY = 'surveyDraft_testForm_12'

    jest.spyOn(Storage.prototype, 'getItem')

    render(<SurveyEditorView
      currentForm={{
        id: 'testForm',
        version: 12,
        content: '{}',
        stableId: 'testStableId',
        name: '',
        createdAt: 0,
        lastUpdatedAt: 0
      }}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)

    //Assert
    expect(localStorage.getItem).toHaveBeenCalledWith(FORM_DRAFT_KEY)
    expect(screen.queryByText('Survey Draft Loaded')).not.toBeInTheDocument()
  })
})
