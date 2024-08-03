import { render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import { ApiContextT, ApiProvider, emptyApi } from 'src/participant/ApiProvider'
import { useAutosaveEffect } from 'src/autoSaveUtils'
import { setupRouterTest } from 'src/test-utils/router-testing-utils'
import { MockI18nProvider } from 'src/participant/i18n-testing-utils'
import { PagedSurveyView } from 'src/components/forms/PagedSurveyView'
import React from 'react'
import {
  generateThreePageSurvey, mockConfiguredSurvey, mockEnrollee, mockHubResponse, mockProfile,
  mockSurveyWithHiddenQuestion,
  mockSurveyWithHiddenQuestionClearOnHidden
} from 'src/test-utils/mocking-utils'
import { Survey } from 'src/types/forms'
import { Profile } from 'src/types/user'

jest.mock('src/autoSaveUtils', () => {
  return {
    useAutosaveEffect: jest.fn()
  }
})

describe('PagedSurveyView', () => {
  it('allows a user to complete the survey', async () => {
    const { submitSpy } = setupSurveyTest(generateThreePageSurvey())
    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await userEvent.type(screen.getByText('text input'), 'my Text')
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page3')).toBeInTheDocument()
    await userEvent.click(screen.getByText('Complete'))
    expect(submitSpy).toHaveBeenCalledTimes(1)
    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { questionStableId: 'text1', stringValue: 'my Text', viewedLanguage: 'en' },
          { questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' }],
        complete: true,
        resumeData: '{"user1":{"currentPageNo":1}}'
      })
    }))
  })
  //
  it('autosaves question and page progress', async () => {
    const profile = { sexAtBirth: 'male' }

    const { submitSpy, triggerAutosave } = setupSurveyTest(generateThreePageSurvey(), profile)

    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await userEvent.type(screen.getByText('text input'), 'my Text')
    await userEvent.click(screen.getByText('Next'))
    triggerAutosave()
    triggerAutosave()
    // should only have been called once, despite multiple intervals passing, since it only is called on diffs
    expect(submitSpy).toHaveBeenCalledTimes(1)
    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: expect.arrayContaining([{ questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' },
          { questionStableId: 'text1', stringValue: 'my Text', viewedLanguage: 'en' }]),
        complete: false,
        resumeData: '{"user1":{"currentPageNo":3}}'
      })
    }))
  })

  it('autosaves question and page progress with diffs', async () => {
    const profile = { sexAtBirth: 'male' }

    const { submitSpy, triggerAutosave } = setupSurveyTest(generateThreePageSurvey(), profile)

    await userEvent.click(screen.getByText('Green'))
    triggerAutosave()
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await userEvent.type(screen.getByText('text input'), 'my Text')
    await userEvent.click(screen.getByText('Next'))
    triggerAutosave()

    expect(submitSpy).toHaveBeenCalledTimes(2)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'text1', stringValue: 'my Text', viewedLanguage: 'en' }]
      })
    }))
  })

  it('autosave handles updated questions', async () => {
    const profile = { sexAtBirth: 'male' }

    const { submitSpy, triggerAutosave } = setupSurveyTest(generateThreePageSurvey(), profile)
    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    triggerAutosave()
    await userEvent.click(screen.getByText('Previous'))
    await userEvent.click(screen.getByText('Blue'))
    triggerAutosave()

    expect(submitSpy).toHaveBeenCalledTimes(2)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'blue', viewedLanguage: 'en' },
          { questionStableId: 'colorCode', stringValue: '#00F', viewedLanguage: 'en' }]
      })
    }))
  })

  it('autosave handles hidden questions with default clear-on-submit behavior', async () => {
    const profile = { sexAtBirth: 'male' }

    const { submitSpy, triggerAutosave } = setupSurveyTest(mockSurveyWithHiddenQuestion(), profile)
    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('forest green'))
    triggerAutosave()
    await userEvent.click(screen.getByText('Blue'))
    triggerAutosave()
    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenCalledTimes(3)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'questionStableId': 'greenFollow', 'stringValue': 'forest', viewedLanguage: 'en' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'blue', viewedLanguage: 'en' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(3, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'greenFollow', viewedLanguage: 'en' }]
      })
    }))
  })

  it('autosave handles hidden questions with clear-on-hidden', async () => {
    const profile = { sexAtBirth: 'male' }

    const { submitSpy, triggerAutosave } = setupSurveyTest(
      mockSurveyWithHiddenQuestionClearOnHidden(),
      profile
    )
    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('forest green'))
    triggerAutosave()
    await userEvent.click(screen.getByText('Blue'))
    triggerAutosave()
    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'questionStableId': 'greenFollow', 'stringValue': 'forest', viewedLanguage: 'en' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'blue', viewedLanguage: 'en' },
          { questionStableId: 'greenFollow', viewedLanguage: 'en' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(3, expect.objectContaining({
      response: expect.objectContaining({
        answers: []
      })
    }))
  })

  it('retries autosave if autosave fails', async () => {
    const profile = { sexAtBirth: 'male' }

    const { submitSpy, triggerAutosave } = setupSurveyTest(generateThreePageSurvey(), profile)
    submitSpy.mockImplementation(() => Promise.reject({}))

    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    triggerAutosave()
    // we need a small wait for the error state to propagate.the update has no impact on the DOM, so we wait manually
    await new Promise(r => setTimeout(r, 100))
    triggerAutosave()
    const expectedDiffResponse = expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' }],
        complete: false,
        resumeData: '{"user1":{"currentPageNo":2}}'
      })
    })
    expect(submitSpy).toHaveBeenNthCalledWith(1, expectedDiffResponse)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({ alertErrors: true }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expectedDiffResponse)
    // confirm it doesn't spam the user with alerts
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({ alertErrors: false }))
  })
})

/**
 *
 */
const setupSurveyTest = (survey: Survey, profile?: Profile) => {
  const mockUpdateSurveyResponse = jest.fn().mockResolvedValue(mockHubResponse())

  const mockApi: ApiContextT = {
    ...emptyApi,
    updateSurveyResponse: mockUpdateSurveyResponse
  }

  const autosaveManager = {
    trigger: (): void => { throw 'no autosave registered' }
  };

  (useAutosaveEffect as jest.Mock).mockImplementation(saveFn => {
    autosaveManager.trigger = () => { saveFn() }
  })
  const triggerAutosave = () => autosaveManager.trigger()

  const configuredSurvey = {
    ...mockConfiguredSurvey(),
    survey
  }

  const enrollee = {
    ...mockEnrollee(),
    profile: profile || mockProfile()
  }

  const { RoutedComponent } = setupRouterTest(
    <ApiProvider api={mockApi}>
      <MockI18nProvider>
        <PagedSurveyView enrollee={enrollee} form={configuredSurvey.survey} response={mockHubResponse().response}
          studyEnvParams={{ studyShortcode: 'study', portalShortcode: 'portal', envName: 'sandbox' }}
          updateResponseMap={jest.fn()}
          selectedLanguage={'en'} updateProfile={jest.fn()} setAutosaveStatus={jest.fn()}
          taskId={'guid34'} adminUserId={null} updateEnrollee={jest.fn()} onFailure={jest.fn()} onSuccess={jest.fn()}/>
      </MockI18nProvider>
    </ApiProvider>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  return { submitSpy: mockUpdateSurveyResponse, RoutedComponent, triggerAutosave }
}
