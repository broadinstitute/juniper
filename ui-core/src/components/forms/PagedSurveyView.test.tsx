import {
  render,
  screen
} from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import {
  ApiContextT,
  ApiProvider,
  emptyApi
} from 'src/participant/ApiProvider'
import { useAutosaveEffect } from 'src/autoSaveUtils'
import { setupRouterTest } from 'src/test-utils/router-testing-utils'
import { MockI18nProvider } from 'src/participant/i18n-testing-utils'
import { PagedSurveyView } from 'src/components/forms/PagedSurveyView'
import React from 'react'
import {
  generateSurvey,
  generateSurveyWithQuestion,
  generateThreePageSurvey,
  mockConfiguredSurvey,
  mockEnrollee,
  mockHubResponse,
  mockProfile,
  mockSurveyWithHiddenQuestion,
  mockSurveyWithHiddenQuestionClearOnHidden
} from 'src/test-utils/mocking-utils'
import {
  Answer,
  Survey
} from 'src/types/forms'
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
    await userEvent.type(screen.getByLabelText('2. text input'), 'my Text')
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page3')).toBeInTheDocument()
    await userEvent.click(screen.getByText('Complete'))
    expect(submitSpy).toHaveBeenCalledTimes(1)
    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'format': 'NONE', questionStableId: 'text1', stringValue: 'my Text', viewedLanguage: 'en' },
          { 'format': 'NONE', questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' }
        ],
        complete: true,
        resumeData: '{"user1":{"currentPageNo":1}}'
      })
    }))
  })

  it('passes down referenced answers as variables', async () => {
    setupSurveyTest({
      ...generateSurvey(),
      content: `{
      "pages":[
        {"elements":[
          {"type":"html","html":"You are on page1"},
          {"type":"text","name":"test","title":"test {otherSurvey.question1}"}
        ]},
        {"elements":[{"type":"text","name":"otherPage","title":"otherpage"}]}
      ]
      }`
    },
    undefined,
    [{
      format: 'NONE',
      surveyStableId: 'otherSurvey',
      questionStableId: 'question1',
      stringValue: 'my custom response'
    }])

    expect(screen.getByText('test my custom response')).toBeInTheDocument()
  })
  //
  it('autosaves question and page progress', async () => {
    const profile = mockProfile()

    const { submitSpy, triggerAutosave } = setupSurveyTest(generateThreePageSurvey(), profile)

    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await userEvent.type(screen.getByLabelText('2. text input'), 'my Text')
    await userEvent.click(screen.getByText('Next'))
    triggerAutosave()
    triggerAutosave()
    // should only have been called once, despite multiple intervals passing, since it only is called on diffs
    expect(submitSpy).toHaveBeenCalledTimes(1)

    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: expect.arrayContaining([
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'format': 'NONE', questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' },
          { 'format': 'NONE', questionStableId: 'text1', stringValue: 'my Text', viewedLanguage: 'en' }
        ]),
        complete: false,
        resumeData: '{"user1":{"currentPageNo":3}}'
      })
    }))
  })

  it('autosaves question and page progress with diffs', async () => {
    const profile = mockProfile()

    const { submitSpy, triggerAutosave } = setupSurveyTest(generateThreePageSurvey(), profile)

    await userEvent.click(screen.getByText('Green'))
    triggerAutosave()
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await userEvent.type(screen.getByLabelText('2. text input'), 'my Text')
    await userEvent.click(screen.getByText('Next'))
    triggerAutosave()

    expect(submitSpy).toHaveBeenCalledTimes(2)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'format': 'NONE',  questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' }
        ]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ 'format': 'NONE', questionStableId: 'text1', stringValue: 'my Text', viewedLanguage: 'en' }]
      })
    }))
  })

  it('autosave handles updated questions', async () => {
    const profile = mockProfile()

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
        answers: [
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'format': 'NONE', questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' }
        ]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'blue', viewedLanguage: 'en' },
          { 'format': 'NONE', questionStableId: 'colorCode', stringValue: '#00F', viewedLanguage: 'en' }
        ]
      })
    }))
  })

  it('autosave handles hidden questions with default clear-on-submit behavior', async () => {
    const profile = mockProfile()

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
        answers: [
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'format': 'NONE', 'questionStableId': 'greenFollow', 'stringValue': 'forest', viewedLanguage: 'en' }
        ]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ 'format': 'NONE', questionStableId: 'radio1', stringValue: 'blue', viewedLanguage: 'en' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(3, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ 'format': 'NONE', questionStableId: 'greenFollow', viewedLanguage: 'en' }]
      })
    }))
  })

  it('autosave handles hidden questions with clear-on-hidden', async () => {
    const profile = mockProfile()

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
        answers: [
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'format': 'NONE', 'questionStableId': 'greenFollow', 'stringValue': 'forest', viewedLanguage: 'en' }
        ]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'blue', viewedLanguage: 'en' },
          { 'format': 'NONE', questionStableId: 'greenFollow', viewedLanguage: 'en' }
        ]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(3, expect.objectContaining({
      response: expect.objectContaining({
        answers: []
      })
    }))
  })

  it('retries autosave if autosave fails', async () => {
    const profile = mockProfile()

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
        answers: [
          { 'format': 'NONE', questionStableId: 'radio1', stringValue: 'green', viewedLanguage: 'en' },
          { 'format': 'NONE', questionStableId: 'colorCode', stringValue: '#0F0', viewedLanguage: 'en' }
        ],
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
  it('submits text questions', async () => {
    const survey = generateSurveyWithQuestion({
      type: 'text',
      name: 'myTextQuestion',
      title: 'my question'
    })

    const { submitSpy } = setupSurveyTest(survey)

    await userEvent.type(screen.getByLabelText('1. my question'), 'really smart response')
    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenCalledTimes(1)


    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [
          {
            format: 'NONE', questionStableId: 'myTextQuestion',
            stringValue: 'really smart response', viewedLanguage: 'en'
          }
        ]
      })
    }))
  })

  it('submits radiogroup questions', async () => {
    const survey = generateSurveyWithQuestion({
      type: 'radiogroup',
      choices: [
        { text: 'Choice 1', value: 'choice1' },
        { text: 'Choice 2', value: 'choice2' }
      ],
      name: 'myRadioQuestion',
      title: 'my radio question'
    })

    const { submitSpy } = setupSurveyTest(survey)

    await userEvent.click(screen.getByText('Choice 2'))
    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenCalledTimes(1)


    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ format: 'NONE', questionStableId: 'myRadioQuestion', stringValue: 'choice2', viewedLanguage: 'en' }]
      })
    }))
  })

  it('submits dropdown questions', async () => {
    const survey = generateSurveyWithQuestion({
      type: 'dropdown',
      choices: [
        { text: 'Choice 1', value: 'choice1' },
        { text: 'Choice 2', value: 'choice2' }
      ],
      name: 'myDropdownQuestion',
      title: 'my dropdown question'
    })

    const { submitSpy } = setupSurveyTest(survey)

    // it's appropriately labelled for accessibility, but for some reason it was also selecting the label
    await userEvent.click(screen.getByPlaceholderText('Select...'))
    await userEvent.click(screen.getByText('Choice 2'))
    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenCalledTimes(1)


    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [
          { format: 'NONE', questionStableId: 'myDropdownQuestion', stringValue: 'choice2', viewedLanguage: 'en' }
        ]
      })
    }))
  })

  it('submits checkbox questions', async () => {
    const survey = generateSurveyWithQuestion({
      type: 'checkbox',
      choices: [
        { text: 'Choice 1', value: 'choice1' },
        { text: 'Choice 2', value: 'choice2' },
        { text: 'Choice 3', value: 'choice3' }
      ],
      name: 'myCheckboxQuestion',
      title: 'my checkbox question'
    })

    const { submitSpy } = setupSurveyTest(survey)

    await userEvent.click(screen.getByText('Choice 2'))
    await userEvent.click(screen.getByText('Choice 1'))
    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenCalledTimes(1)


    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [{
          format: 'NONE',
          questionStableId: 'myCheckboxQuestion',
          objectValue: '["choice2","choice1"]',
          viewedLanguage: 'en'
        }]
      })
    }))
  })

  it('submits panelDynamic questions', async () => {
    const survey = generateSurveyWithQuestion({
      type: 'paneldynamic',
      templateElements: [
        {
          type: 'text',
          title: 'My nested text question',
          name: 'myNestedTextQuestion'
        },
        {
          type: 'text',
          title: 'My other nested text question',
          name: 'myOtherNestedTextQuestion'
        }
      ],
      name: 'myPanelDynamicQuestion',
      title: 'my panel dynamic question'
    })

    const { submitSpy } = setupSurveyTest(survey)

    await userEvent.click(screen.getByText('Add new'))
    await userEvent.type(screen.getByLabelText('My nested text question'), 'answer 1')
    await userEvent.type(screen.getByLabelText('My other nested text question'), 'answer 2')
    await userEvent.click(screen.getByText('Add new'))
    await userEvent.type(screen.getAllByLabelText('My nested text question')[1], 'answer 3')
    await userEvent.type(screen.getAllByLabelText('My other nested text question')[1], 'answer 4')

    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenCalledTimes(1)


    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [{
          format: 'NONE',
          questionStableId: 'myPanelDynamicQuestion',
          objectValue: '[{"myNestedTextQuestion":"answer 1","myOtherNestedTextQuestion":"answer 2"},' +
            '{"myNestedTextQuestion":"answer 3","myOtherNestedTextQuestion":"answer 4"}]',
          viewedLanguage: 'en'
        }]
      })
    }))
  })
})

/**
 *
 */
const setupSurveyTest = (survey: Survey, profile?: Profile, referencedAnswers?: Answer[]) => {
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
          updateResponseMap={jest.fn()} referencedAnswers={referencedAnswers || []}
          selectedLanguage={'en'} updateProfile={jest.fn()} setAutosaveStatus={jest.fn()}
          taskId={'guid34'} adminUserId={null} updateEnrollee={jest.fn()} onFailure={jest.fn()} onSuccess={jest.fn()}/>
      </MockI18nProvider>
    </ApiProvider>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  return { submitSpy: mockUpdateSurveyResponse, RoutedComponent, triggerAutosave }
}
