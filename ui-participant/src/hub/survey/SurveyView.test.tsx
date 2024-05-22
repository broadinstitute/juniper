import React from 'react'

import {
  generateThreePageSurvey
} from 'test-utils/test-survey-factory'
import { render, screen } from '@testing-library/react'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import {
  asMockedFn,
  MockI18nProvider,
  PageNumberControl,
  Survey,
  SurveyFooter, useSurveyJSModel
} from '@juniper/ui-core'
import { mockUseActiveUser, mockUseUser } from 'test-utils/user-mocking-utils'
import { useActiveUser } from 'providers/ActiveUserProvider'

jest.mock('providers/PortalProvider', () => ({ usePortalEnv: jest.fn() }))

// mock the useAutosaveEffect, but leave other core functions intact
jest.mock('@juniper/ui-core', () => {
  const original = jest.requireActual('@juniper/ui-core')
  return {
    ...original,
    useAutosaveEffect: jest.fn()
  }
})

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

jest.mock('providers/ActiveUserProvider')


const FooterTestComponent = ({ pageNum, survey }: {pageNum: number, survey: Survey}) => {
  const pager: PageNumberControl = { pageNumber: pageNum, updatePageNumber: () => 1 }
  const { surveyModel } = useSurveyJSModel(survey, null,
    () => 1, pager, 'sandbox')
  return <SurveyFooter survey={survey} surveyModel={surveyModel}/>
}


describe('SurveyFooter', () => {
  it('does not render if not on the last page', () => {
    asMockedFn(useUser).mockReturnValue(mockUseUser(false))
    asMockedFn(useActiveUser).mockReturnValue({
      ...mockUseActiveUser(),
      profile: { sexAtBirth: 'male' }
    })

    const survey = generateThreePageSurvey({ footer: 'footer stuff' })
    render(<MockI18nProvider>
      <FooterTestComponent survey={survey} pageNum={1}/>
    </MockI18nProvider>)
    expect(screen.queryByText('footer stuff')).toBeNull()
  })

  it('renders if on the last page', () => {
    asMockedFn(useUser).mockReturnValue(mockUseUser(false))
    asMockedFn(useActiveUser).mockReturnValue({
      ...mockUseActiveUser(),
      profile: { sexAtBirth: 'male' }
    })

    const survey = generateThreePageSurvey({ footer: 'footer stuff' })
    render(
      <MockI18nProvider>
        <FooterTestComponent survey={survey} pageNum={3}/>
      </MockI18nProvider>)
    expect(screen.queryByText('footer stuff')).toBeTruthy()
  })
})


