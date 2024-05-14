import React from 'react'
import { render, screen } from '@testing-library/react'
import { SurveyAutoCompleteButton } from './SurveyAutoCompleteButton'

describe('SurveyAutoCompleteButton', () => {
  it('should render in sandbox environment', () => {
    render(<SurveyAutoCompleteButton surveyModel={null} envName={'sandbox'}/>)
    expect(screen.getByLabelText('automatically fill in the survey')).toBeInTheDocument()
  })

  it('should not render in live environment', () => {
    render(<SurveyAutoCompleteButton surveyModel={null} envName={'live'}/>)
    expect(screen.queryByLabelText('automatically fill in the survey')).not.toBeInTheDocument()
  })
})
