import React from 'react'
import { render, screen } from '@testing-library/react'
import SurveyAutoCompleteButton from './SurveyAutoCompleteButton'
import { getEnvSpec } from 'api/api'
import { asMockedFn } from '@juniper/ui-core'

jest.mock('api/api')

describe('SurveyAutoCompleteButton', () => {
  it('should render in sandbox environment', () => {
    asMockedFn(getEnvSpec).mockReturnValue({
      envName: 'sandbox',
      shortcodeOrHostname: 'demo',
      shortcode: 'demo'
    })
    render(<SurveyAutoCompleteButton surveyModel={null} />)
    expect(screen.getByLabelText('automatically fill in the survey')).toBeInTheDocument()
  })

  it('should not render in live environment', () => {
    asMockedFn(getEnvSpec).mockReturnValue({
      envName: 'live',
      shortcodeOrHostname: 'demo',
      shortcode: 'demo'
    })
    render(<SurveyAutoCompleteButton surveyModel={null} />)
    expect(screen.queryByLabelText('automatically fill in the survey')).not.toBeInTheDocument()
  })
})
