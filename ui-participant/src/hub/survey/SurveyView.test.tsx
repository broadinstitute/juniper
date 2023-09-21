import React from 'react'

import {
  generateThreePageSurvey,
  mockConfiguredSurvey,
  mockSurveyWithHiddenQuestion, mockSurveyWithHiddenQuestionClearOnHidden
} from 'test-utils/test-survey-factory'
import { PageNumberControl, useSurveyJSModel } from 'util/surveyJsUtils'
import { render, screen } from '@testing-library/react'
import { PagedSurveyView, SurveyFooter } from './SurveyView'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { Survey } from '@juniper/ui-core'
import Api from 'api/api'
import { mockEnrollee, mockHubResponse } from 'test-utils/test-participant-factory'
import userEvent from '@testing-library/user-event'
import { setupRouterTest } from 'test-utils/router-testing-utils'

jest.mock('providers/PortalProvider', () => ({ usePortalEnv: jest.fn() }))
beforeEach(() => {
  (usePortalEnv as jest.Mock).mockReturnValue({
    portal: { name: 'demo' },
    portalEnv: {
      environmentName: 'sandbox'
    }
  })
})

jest.mock('providers/UserProvider', () => ({ useUser: jest.fn() }))
beforeEach(() => {
  (useUser as jest.Mock).mockReturnValue({
    updateEnrollee: jest.fn()
  })
})

const FooterTestComponent = ({ pageNum, survey }: {pageNum: number, survey: Survey}) => {
  const pager: PageNumberControl = { pageNumber: pageNum, updatePageNumber: () => 1 }
  const { surveyModel } = useSurveyJSModel(survey, null,
    () => 1, pager, { sexAtBirth: 'male' })
  return <SurveyFooter survey={survey} surveyModel={surveyModel}/>
}

describe('SurveyFooter', () => {
  it('does not render if not on the last page', () => {
    const survey = generateThreePageSurvey({ footer: 'footer stuff' })
    render(<FooterTestComponent survey={survey} pageNum={1}/>)
    expect(screen.queryByText('footer stuff')).toBeNull()
  })

  it('renders if on the last page', () => {
    const survey = generateThreePageSurvey({ footer: 'footer stuff' })
    render(<FooterTestComponent survey={survey} pageNum={3}/>)
    expect(screen.queryByText('footer stuff')).toBeTruthy()
  })
})


describe('Renders a survey', () => {
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
        answers: [{ questionStableId: 'radio1', stringValue: 'green' },
          { questionStableId: 'text1', stringValue: 'my Text' },
          { questionStableId: 'colorCode', stringValue: '#0F0' }],
        complete: true,
        resumeData: '{"user1":{"currentPageNo":1}}'
      })
    }))
  })

  it('autosaves question and page progress', async () => {
    const { submitSpy } = setupSurveyTest(generateThreePageSurvey())

    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await userEvent.type(screen.getByText('text input'), 'my Text')
    await userEvent.click(screen.getByText('Next'))
    await new Promise(r => setTimeout(r, 600))
    // should only have been called once, despite multiple intervals passing, since it only is called on diffs
    expect(submitSpy).toHaveBeenCalledTimes(1)
    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green' },
          { questionStableId: 'colorCode', stringValue: '#0F0' },
          { questionStableId: 'text1', stringValue: 'my Text' }],
        complete: false,
        resumeData: '{"user1":{"currentPageNo":3}}'
      })
    }))
  })

  it('autosaves question and page progress with diffs', async () => {
    const { submitSpy } = setupSurveyTest(generateThreePageSurvey())

    await userEvent.click(screen.getByText('Green'))
    await new Promise(r => setTimeout(r, 250))
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await userEvent.type(screen.getByText('text input'), 'my Text')
    await userEvent.click(screen.getByText('Next'))
    await new Promise(r => setTimeout(r, 250))

    expect(submitSpy).toHaveBeenCalledTimes(2)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green' },
          { questionStableId: 'colorCode', stringValue: '#0F0' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'text1', stringValue: 'my Text' }]
      })
    }))
  })

  it('autosave handles updated questions', async () => {
    const { submitSpy } = setupSurveyTest(generateThreePageSurvey())
    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    await new Promise(r => setTimeout(r, 250))
    await userEvent.click(screen.getByText('Previous'))
    await userEvent.click(screen.getByText('Blue'))
    await new Promise(r => setTimeout(r, 250))

    expect(submitSpy).toHaveBeenCalledTimes(2)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green' },
          { questionStableId: 'colorCode', stringValue: '#0F0' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'blue' },
          { questionStableId: 'colorCode', stringValue: '#00F' }]
      })
    }))
  })

  it('autosave handles hidden questions with default clear-on-submit behavior', async () => {
    const { submitSpy } = setupSurveyTest(mockSurveyWithHiddenQuestion())
    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('forest green'))
    await new Promise(r => setTimeout(r, 250))
    await userEvent.click(screen.getByText('Blue'))
    await new Promise(r => setTimeout(r, 250))
    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenCalledTimes(3)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green' },
          { 'questionStableId': 'greenFollow', 'stringValue': 'forest' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'blue' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(3, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'greenFollow' }]
      })
    }))
  })

  it('autosave handles hidden questions with clear-on-hidden', async () => {
    const { submitSpy } = setupSurveyTest(mockSurveyWithHiddenQuestionClearOnHidden())
    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('forest green'))
    await new Promise(r => setTimeout(r, 250))
    await userEvent.click(screen.getByText('Blue'))
    await new Promise(r => setTimeout(r, 250))
    await userEvent.click(screen.getByText('Complete'))

    expect(submitSpy).toHaveBeenCalledTimes(3)
    expect(submitSpy).toHaveBeenNthCalledWith(1, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green' },
          { 'questionStableId': 'greenFollow', 'stringValue': 'forest' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(2, expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'blue' }, { questionStableId: 'greenFollow' }]
      })
    }))
    expect(submitSpy).toHaveBeenNthCalledWith(3, expect.objectContaining({
      response: expect.objectContaining({
        answers: []
      })
    }))
  })

  it('retries autosave if autosave fails', async () => {
    const { submitSpy } = setupSurveyTest(generateThreePageSurvey())
    submitSpy.mockImplementation(() => Promise.reject({}))

    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await new Promise(r => setTimeout(r, 500))
    const expectedDiffResponse = expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green' }],
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

const setupSurveyTest = (survey: Survey) => {
  const submitSpy = jest.spyOn(Api, 'updateSurveyResponse')
    .mockImplementation(() => Promise.resolve(mockHubResponse()))
  const configuredSurvey = {
    ...mockConfiguredSurvey(),
    survey
  }
  const { RoutedComponent } = setupRouterTest(
    <PagedSurveyView enrollee={mockEnrollee()} form={configuredSurvey}
      studyShortcode={'study'} taskId={'guid34'} autoSaveInterval={200}/>)
  render(RoutedComponent)
  expect(screen.getByText('You are on page1')).toBeInTheDocument()
  return { submitSpy, RoutedComponent }
}


