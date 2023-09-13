import React from 'react'

import { generateThreePageSurvey, mockConfiguredSurvey } from 'test-utils/test-survey-factory'
import { PageNumberControl, useSurveyJSModel } from 'util/surveyJsUtils'
import { render, screen } from '@testing-library/react'
import { PagedSurveyView, SurveyFooter } from './SurveyView'
import { usePortalEnv } from 'providers/PortalProvider'
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
    const submitSpy = jest.spyOn(Api, 'submitSurveyResponse')
      .mockImplementation(() => Promise.resolve(mockHubResponse()))
    const survey = generateThreePageSurvey()
    const configuredSurvey = {
      ...mockConfiguredSurvey(),
      survey
    }
    const { RoutedComponent } = setupRouterTest(
      <PagedSurveyView enrollee={mockEnrollee()} form={configuredSurvey}
        studyShortcode={'study'} taskId={'guid34'}/>)
    render(RoutedComponent)
    expect(screen.getByText('You are on page1')).toBeInTheDocument()
    await userEvent.click(screen.getByText('Green'))
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page2')).toBeInTheDocument()
    await userEvent.click(screen.getByText('Next'))
    expect(screen.getByText('You are on page3')).toBeInTheDocument()
    await userEvent.click(screen.getByText('Complete'))
    expect(submitSpy).toHaveBeenCalledTimes(1)
    expect(submitSpy).toHaveBeenCalledWith(expect.objectContaining({
      response: expect.objectContaining({
        answers: [{ questionStableId: 'radio1', stringValue: 'green' }],
        complete: true,
        resumeData: '{"user1":{"currentPageNo":1}}'
      })
    }))
  })
})
