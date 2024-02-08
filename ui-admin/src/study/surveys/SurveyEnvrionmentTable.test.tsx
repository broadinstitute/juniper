import React from 'react'
import {
  mockConfiguredSurvey,
  mockStudyEnvParams,
  mockSurvey
} from 'test-utils/mocking-utils'
import { screen } from '@testing-library/react'
import { StudyEnvironmentSurveyNamed } from '@juniper/ui-core'
import SurveyEnvironmentTable from './SurveyEnvironmentTable'
import { renderWithRouter } from '../../test-utils/router-testing-utils'

describe('SurveyEnvironmentTest', () => {
  test('shows survey name and versions', async () => {
    const configuredSurveys: StudyEnvironmentSurveyNamed[] = [{
      ...mockConfiguredSurvey(),
      envName: 'sandbox',
      survey: {
        ...mockSurvey(),
        version: 1
      }
    }, {
      ...mockConfiguredSurvey(),
      envName: 'irb',
      survey: {
        ...mockSurvey(),
        version: 2
      }
    }]

    renderWithRouter(<SurveyEnvironmentTable stableIds={[configuredSurveys[0].survey.stableId]}
      studyEnvParams={mockStudyEnvParams()}
      configuredSurveys={configuredSurveys}
      setSelectedSurveyConfig={jest.fn()}
      showDeleteSurveyModal={false}
      setShowDeleteSurveyModal={jest.fn()}
      showArchiveSurveyModal={false}
      setShowArchiveSurveyModal={jest.fn()}
      updateConfiguredSurvey={jest.fn()}
    />)

    // button should appear since there are participants assigned to version 1
    expect(screen.getByText(configuredSurveys[0].survey.name)).toBeInTheDocument()
  })
})
