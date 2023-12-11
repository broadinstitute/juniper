import { render, screen } from '@testing-library/react'
import React from 'react'
import CreateSurveyModal from './CreateSurveyModal'
import { mockConfiguredSurvey, mockStudyEnvContext, mockSurvey } from 'test-utils/mocking-utils'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import userEvent from '@testing-library/user-event'
import Api from 'api/api'

describe('CreateSurveyModal', () => {
  test('disables Create button when survey name and stable ID are blank', () => {
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext} type={'RESEARCH'}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const createButton = screen.getByText('Create')
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when survey name and stable ID are filled out', async () => {
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext} type={'RESEARCH'}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Name')
    const surveyStableIdInput = screen.getByLabelText('Stable ID')
    await user.type(surveyNameInput, 'Test Survey')
    await user.type(surveyStableIdInput, 'test_survey_id')

    const createButton = screen.getByText('Create')
    expect(createButton).toBeEnabled()
  })

  test('should autofill the stable ID as the user fills in the survey name', async () => {
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext} type={'RESEARCH'}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Name')
    const surveyStableIdInput = screen.getByLabelText('Stable ID')
    await user.type(surveyNameInput, 'Test Survey')

    //Confirm that auto-fill stable ID worked
    expect(surveyStableIdInput).toHaveValue('testSurvey')
  })

  test('create a required survey', async () => {
    jest.spyOn(window, 'alert').mockImplementation(jest.fn())
    const survey = mockSurvey()
    jest.spyOn(Api, 'createConfiguredSurvey').mockImplementation(() => Promise.resolve(mockConfiguredSurvey()))
    jest.spyOn(Api, 'createNewSurvey').mockImplementation(() => Promise.resolve(survey))
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext} type={'RESEARCH'}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Name')
    const surveyStableIdInput = screen.getByLabelText('Stable ID')
    const requiredCheckbox = screen.getByLabelText('Required')
    await user.type(surveyNameInput, survey.name)
    await user.type(surveyStableIdInput, survey.stableId)
    await user.click(requiredCheckbox)
    await user.click(screen.getByText('Create'))

    expect(Api.createConfiguredSurvey).toHaveBeenCalledWith(studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName,
      {
        allowAdminEdit: true,
        allowParticipantReedit: true,
        allowParticipantStart: true,
        id: '',
        required: true,
        prepopulate: false,
        recurrenceIntervalDays: 0,
        recur: false,
        studyEnvironmentId: studyEnvContext.currentEnv.id,
        survey: {
          content: '{}',
          createdAt: 0,
          id: survey.id,
          lastUpdatedAt: 0,
          name: survey.name,
          surveyType: 'RESEARCH',
          stableId: survey.stableId,
          version: 1
        },
        surveyId: survey.id,
        surveyOrder: 1
      }
    )
  })
})
