import React from 'react'

import { generateThreePageSurvey } from 'test-utils/test-survey-factory'
import { PageNumberControl, useSurveyJSModel } from 'util/surveyJsUtils'
import { render, screen } from '@testing-library/react'
import { SurveyFooter } from './SurveyView'
import { usePortalEnv } from 'providers/PortalProvider'
import { Survey } from '@juniper/ui-core'

jest.mock('providers/PortalProvider', () => ({ usePortalEnv: jest.fn() }))
beforeEach(() => {
  (usePortalEnv as jest.Mock).mockReturnValue({
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
