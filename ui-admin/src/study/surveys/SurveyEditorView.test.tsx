import React from 'react'
import { screen } from '@testing-library/react'
import SurveyEditorView from './SurveyEditorView'
import { getFormDraftKey } from 'forms/designer/utils/formDraftUtils'
import { defaultSurvey, renderWithRouter, Survey } from '@juniper/ui-core'
import {
  mockExpressionApis,
  mockStudyEnvContext, mockTwoLanguagePortal, renderInPortalRouter
} from 'test-utils/mocking-utils'
import { userEvent } from '@testing-library/user-event'
import { select } from 'react-select-event'
import clearAllMocks = jest.clearAllMocks

describe('SurveyEditorView', () => {
  const mockForm = ():Survey => ({
    ...defaultSurvey,
    createdAt: 0,
    lastUpdatedAt: 0,
    id: 'testForm',
    version: 12,
    content: '{}',
    stableId: 'testStableId',
    name: 'Test survey',
    surveyType: 'RESEARCH',
    recurrenceType: 'NONE'
  })

  test('shows the user a LoadedLocalDraftModal when a draft is loaded', async () => {
    //Arrange
    const FORM_DRAFT_KEY = getFormDraftKey({ form: mockForm() })
    localStorage.setItem(FORM_DRAFT_KEY, JSON.stringify({}))

    jest.spyOn(Storage.prototype, 'getItem')
    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm()}
      onCancel={jest.fn()}
      onSave={jest.fn()}
      replaceSurvey={jest.fn()}
    />)

    //Assert
    const modalHeader = screen.getByText('Survey Draft Loaded')
    expect(modalHeader).toBeInTheDocument()
  })

  test('checks local storage for a draft', async () => {
    clearAllMocks()
    localStorage.clear()
    //Arrange
    const FORM_DRAFT_KEY = getFormDraftKey({ form: mockForm() })

    jest.spyOn(Storage.prototype, 'getItem')

    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm()}
      onCancel={jest.fn()}
      onSave={jest.fn()}
      replaceSurvey={jest.fn()}
    />)

    //Assert
    expect(localStorage.getItem).toHaveBeenCalledWith(FORM_DRAFT_KEY)
    expect(screen.queryByText('Survey Draft Loaded')).not.toBeInTheDocument()
  })

  test('shows a dropdown with options', async () => {
    mockExpressionApis()
    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm()}
      onCancel={jest.fn()}
      onSave={jest.fn()}
      replaceSurvey={jest.fn()}
    />)
    expect(screen.getByLabelText('form options menu')).toBeInTheDocument()
    await userEvent.click(screen.getByLabelText('form options menu'))
    await userEvent.click(screen.getByText('Configuration'))
    expect(screen.getByText(`${mockForm().name} - configuration`)).toBeInTheDocument()
  })

  test('allows the user to download the JSON file', async () => {
    mockExpressionApis()
    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm()}
      onCancel={jest.fn()}
      onSave={jest.fn()}
      replaceSurvey={jest.fn()}
    />)
    await userEvent.click(screen.getByLabelText('form options menu'))
    expect(screen.getByText('Download form JSON')).toBeInTheDocument()
  })

  test('shows stableId and version', async () => {
    const stableId = 'testStableId'
    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={{ ...mockForm(), stableId }}
      onCancel={jest.fn()}
      onSave={jest.fn()}
      replaceSurvey={jest.fn()}
    />)
    expect(screen.getByText('testStableId v12')).toBeInTheDocument()
    expect(screen.queryByText('published')).not.toBeInTheDocument()
  })

  test('shows published version if applicable', async () => {
    const stableId = 'testStableId'
    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={{ ...mockForm(), stableId, publishedVersion: 2 }}
      onCancel={jest.fn()}
      onSave={jest.fn()}
      replaceSurvey={jest.fn()}
    />)
    expect(screen.getByText('testStableId v12')).toBeInTheDocument()
    expect(screen.getByText('- published v2')).toBeInTheDocument()
  })

  test('toggles languages', async () => {
    Element.prototype.scrollIntoView = jest.fn()
    const portal = mockTwoLanguagePortal()
    renderInPortalRouter(portal,
      <SurveyEditorView
        studyEnvContext={mockStudyEnvContext()}
        currentForm={{
          ...mockForm(), content: JSON.stringify({
            pages: [{
              elements: [{
                type: 'text', name: 'testQ', title: { 'en': 'English question', 'es': 'Español pregunta' }
              }]
            }]
          })
        }}
        onCancel={jest.fn()}
        onSave={jest.fn()}
        replaceSurvey={jest.fn()}
      />)
    await userEvent.click(screen.getAllByText('Designer')[1])

    await userEvent.click(screen.getByText('testQ'))
    expect(screen.getAllByText('English question')).toHaveLength(2)
    expect(screen.queryByText('Español pregunta')).not.toBeInTheDocument()
    await select(screen.getByLabelText('Select a language'), 'Español')
    expect(screen.getAllByText('Español pregunta')).toHaveLength(2)
  })
})

