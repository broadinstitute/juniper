import React from 'react'
import { render, screen } from '@testing-library/react'
import SurveyEditorView from './SurveyEditorView'
import { getFormDraftKey } from 'forms/designer/utils/formDraftUtils'
import { VersionedForm } from '@juniper/ui-core'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { setupRouterTest } from '../../test-utils/router-testing-utils'

describe('SurveyEditorView', () => {
  const mockForm: VersionedForm = {
    id: 'testForm',
    version: 12,
    content: '{}',
    stableId: 'testStableId',
    name: '',
    createdAt: 0,
    lastUpdatedAt: 0
  }

  test('shows the user a LoadedLocalDraftModal when a draft is loaded', async () => {
    //Arrange
    const FORM_DRAFT_KEY = getFormDraftKey({ form: mockForm })
    localStorage.setItem(FORM_DRAFT_KEY, JSON.stringify({}))

    jest.spyOn(Storage.prototype, 'getItem')
    const { RoutedComponent } = setupRouterTest(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)
    render(RoutedComponent)

    //Assert
    const modalHeader = screen.getByText('Survey Draft Loaded')
    expect(modalHeader).toBeInTheDocument()
  })

  test('checks local storage for a draft', async () => {
    //Arrange
    const FORM_DRAFT_KEY = getFormDraftKey({ form: mockForm })

    jest.spyOn(Storage.prototype, 'getItem')

    const { RoutedComponent } = setupRouterTest(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)
    render(RoutedComponent)

    //Assert
    expect(localStorage.getItem).toHaveBeenCalledWith(FORM_DRAFT_KEY)
    expect(screen.queryByText('Survey Draft Loaded')).not.toBeInTheDocument()
  })

  test('allows the user to download the JSON file', async () => {
    //Arrange
    const { RoutedComponent } = setupRouterTest(<SurveyEditorView
      studyEnvContext={mockStudyEnvContext()}
      currentForm={mockForm}
      onCancel={jest.fn()}
      onSave={jest.fn()}
    />)
    render(RoutedComponent)

    //Act
    const downloadButton = screen.getByRole('button', { name: 'Download JSON' })
    downloadButton.click()

    //Assert
    expect(downloadButton).toBeInTheDocument()
    expect(downloadButton).toBeEnabled()
  })
})
