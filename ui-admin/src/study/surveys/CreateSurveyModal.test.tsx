import { act, render, screen } from '@testing-library/react'
import React from 'react'
import CreateSurveyModal from './CreateSurveyModal'
import { mockConfiguredSurvey, mockExpressionApis, mockStudyEnvContext, mockSurvey } from 'test-utils/mocking-utils'
import { userEvent } from '@testing-library/user-event'
import Api from 'api/api'
import { defaultSurvey, setupRouterTest } from '@juniper/ui-core'

describe('CreateSurveyModal', () => {
  test('disables Create button when survey name and stable ID are blank', async () => {
    mockExpressionApis()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext} type={'RESEARCH'}
      onDismiss={jest.fn()}/>)
    await act(() => render(RoutedComponent))

    const createButton = screen.getByText('Create')
    expect(createButton).toBeDisabled()
  })

  test('enables Create button when survey name and stable ID are filled out', async () => {
    const user = userEvent.setup()
    mockExpressionApis()
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
    mockExpressionApis()
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

  test('outreach surveys should present a blurb input', async () => {
    const user = userEvent.setup()
    mockExpressionApis()
    const studyEnvContext = mockStudyEnvContext()
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext} type={'OUTREACH'}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Name')
    const surveyStableIdInput = screen.getByLabelText('Stable ID')
    const outreachBlurbInput = screen.getByLabelText('Blurb')
    await user.type(surveyNameInput, 'Test Survey')
    await user.type(surveyStableIdInput, 'test_survey_id')
    await user.type(outreachBlurbInput, 'Test Blurb')

    const createButton = screen.getByText('Create')
    expect(createButton).toBeEnabled()
  })

  test('outreach surveys should allow creating marketing types', async () => {
    mockExpressionApis()
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const survey = mockSurvey()
    jest.spyOn(Api, 'createNewSurvey').mockResolvedValue(survey)
    jest.spyOn(Api, 'createConfiguredSurvey').mockResolvedValue(mockConfiguredSurvey())
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext} type={'OUTREACH'}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Name')
    const outreachBlurbInput = screen.getByLabelText('Blurb')
    const marketingCheckbox = screen.getByText('Marketing')
    await user.type(surveyNameInput, 'Test Marketing')
    await user.type(outreachBlurbInput, 'Testing out the marketing blurb...')
    await user.click(marketingCheckbox)

    const createButton = screen.getByText('Create')
    expect(createButton).toBeEnabled()

    await user.click(createButton)
    expect(Api.createNewSurvey).toHaveBeenCalledWith(studyEnvContext.portal.shortcode,
      {
        ...defaultSurvey,
        autoUpdateTaskAssignments: true,
        assignToExistingEnrollees: true,
        blurb: 'Testing out the marketing blurb...',
        content: expect.stringContaining('{"pages":[{"elements":[{"type":"html","name":"outreach_content_'),
        createdAt: expect.anything(),
        lastUpdatedAt: expect.anything(),
        eligibilityRule: '',
        id: '',
        name: 'Test Marketing',
        stableId: 'testMarketing',
        surveyType: 'OUTREACH',
        version: 1
      })
  })

  test('outreach surveys should allow creating screener types', async () => {
    mockExpressionApis()
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    const survey = mockSurvey()
    jest.spyOn(Api, 'createNewSurvey').mockResolvedValue(survey)
    jest.spyOn(Api, 'createConfiguredSurvey').mockResolvedValue(mockConfiguredSurvey())
    const { RoutedComponent } = setupRouterTest(<CreateSurveyModal
      studyEnvContext={studyEnvContext} type={'OUTREACH'}
      onDismiss={jest.fn()}/>)
    render(RoutedComponent)

    const surveyNameInput = screen.getByLabelText('Name')
    const outreachBlurbInput = screen.getByLabelText('Blurb')
    const screenerSelect = screen.getByText('Screener')
    await user.type(surveyNameInput, 'Test Screener')
    await user.type(outreachBlurbInput, 'Testing out the screener blurb...')
    await user.click(screenerSelect)

    const createButton = screen.getByText('Create')
    expect(createButton).toBeEnabled()

    await user.click(createButton)
    expect(Api.createNewSurvey).toHaveBeenCalledWith(studyEnvContext.portal.shortcode,
      {
        ...defaultSurvey,
        autoUpdateTaskAssignments: true,
        blurb: 'Testing out the screener blurb...',
        assignToExistingEnrollees: true,
        content: '{"pages":[{"elements":[]}]}',
        createdAt: expect.anything(),
        lastUpdatedAt: expect.anything(),
        id: '',
        eligibilityRule: '',
        name: 'Test Screener',
        stableId: 'testScreener',
        surveyType: 'OUTREACH',
        version: 1
      })
  })

  test('create a required survey', async () => {
    mockExpressionApis()
    const survey = mockSurvey()
    jest.spyOn(Api, 'createConfiguredSurvey').mockResolvedValue(mockConfiguredSurvey())
    jest.spyOn(Api, 'createNewSurvey').mockResolvedValue(survey)
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

        id: '',
        studyEnvironmentId: studyEnvContext.currentEnv.id,
        survey: {
          ...defaultSurvey,
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
