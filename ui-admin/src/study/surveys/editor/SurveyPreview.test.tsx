import { render, screen } from '@testing-library/react'
import React from 'react'

import { SurveyPreview } from './SurveyPreview'

// TODO: Types for survey config
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const survey: any = {
  title: 'Test survey',
  pages: [
    {
      elements: [
        {
          name: 'test_firstName',
          type: 'text',
          title: 'First name',
          isRequired: true
        },
        {
          name: 'test_lastName',
          type: 'text',
          title: 'Last name',
          isRequired: true
        }
      ]
    }
  ]
}

describe('SurveyPreview', () => {
  // eslint-disable-next-line jest/expect-expect
  it('renders survey', () => {
    // Act
    render(<SurveyPreview survey={survey} />)

    // Assert
    screen.getAllByLabelText('First name')
    screen.getAllByLabelText('Last name')
  })
})
