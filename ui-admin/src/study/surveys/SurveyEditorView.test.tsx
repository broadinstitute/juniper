import React from 'react'
import { screen } from '@testing-library/react'
import SurveyEditorView from './SurveyEditorView'
import { getFormDraftKey } from 'forms/designer/utils/formDraftUtils'
import { defaultSurvey, Survey } from '@juniper/ui-core'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { renderWithRouter } from '../../test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'

describe('SurveyEditorView', () => {
  const mockForm: Survey = {
    ...defaultSurvey,
    createdAt: 0,
    lastUpdatedAt: 0,
    id: 'testForm',
    version: 12,
    content: '{}',
    stableId: 'testStableId',
    name: 'Test survey',
    surveyType: 'RESEARCH'
  }

  test('shows the user a LoadedLocalDraftModal when a draft is loaded', async () => {
    //Arrange
    const FORM_DRAFT_KEY = getFormDraftKey({ form: mockForm })
    localStorage.setItem(FORM_DRAFT_KEY, JSON.stringify({}))

    jest.spyOn(Storage.prototype, 'getItem')
    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)

    //Assert
    const modalHeader = screen.getByText('Survey Draft Loaded')
    expect(modalHeader).toBeInTheDocument()
  })

  test('checks local storage for a draft', async () => {
    //Arrange
    const FORM_DRAFT_KEY = getFormDraftKey({ form: mockForm })

    jest.spyOn(Storage.prototype, 'getItem')

    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)

    //Assert
    expect(localStorage.getItem).toHaveBeenCalledWith(FORM_DRAFT_KEY)
    expect(screen.queryByText('Survey Draft Loaded')).not.toBeInTheDocument()
  })

  test('shows a dropdown with options', async () => {
    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)
    expect(screen.getByLabelText('form options menu')).toBeInTheDocument()
    await userEvent.click(screen.getByLabelText('form options menu'))
    await userEvent.click(screen.getByText('Configuration'))
    expect(screen.getByText(`${mockForm.name} - configuration`)).toBeInTheDocument()
  })

  test('allows the user to download the JSON file', async () => {
    renderWithRouter(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)
    await userEvent.click(screen.getByLabelText('form options menu'))
    expect(screen.getByText('Download form JSON')).toBeInTheDocument()
  })
})
