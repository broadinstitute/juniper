import React from 'react'
import {
  mockConfiguredSurvey,
  mockStudyEnvParams,
  mockSurvey
} from 'test-utils/mocking-utils'
import { screen } from '@testing-library/react'
import { renderWithRouter, StudyEnvironmentSurveyNamed } from '@juniper/ui-core'
import SurveyEnvironmentTable from './SurveyEnvironmentTable'
import { getTableCell } from '../../test-utils/table-testing-utils'
import { userEvent } from '@testing-library/user-event'

describe('SurveyEnvironmentTest', () => {
  test('shows survey name and versions', async () => {
    const configuredSurveys: StudyEnvironmentSurveyNamed[] = [{
      ...mockConfiguredSurvey(),
      envName: 'sandbox',
      survey: {
        ...mockSurvey(),
        version: 2
      }
    }, {
      ...mockConfiguredSurvey(),
      envName: 'irb',
      survey: {
        ...mockSurvey(),
        version: 1
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

    expect(screen.getByText(configuredSurveys[0].survey.name)).toBeInTheDocument()
  })

  test('shows publishing controls', async () => {
    const configuredSurveys: StudyEnvironmentSurveyNamed[] = [{
      ...mockConfiguredSurvey(),
      envName: 'sandbox',
      survey: {
        ...mockSurvey(),
        version: 2
      }
    }, {
      ...mockConfiguredSurvey(),
      envName: 'irb',
      survey: {
        ...mockSurvey(),
        version: 1
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

    expect(screen.getByText(configuredSurveys[0].survey.name)).toBeInTheDocument()
    const sandboxCell = getTableCell(screen.getByRole('table'), configuredSurveys[0].survey.name, 'sandbox', 1)
    await userEvent.click(sandboxCell.querySelector('button[aria-label="Configure survey menu"]')!)
    expect(screen.getByText('Publish to irb')).toBeVisible()

    const irbCell = getTableCell(screen.getByRole('table'), configuredSurveys[0].survey.name, 'sandbox', 1)
    await userEvent.click(irbCell.querySelector('button[aria-label="Configure survey menu"]')!)
    expect(screen.getByText('Publish to live')).toBeVisible()
    await userEvent.click(screen.getByText('Publish to live'))
    expect(screen.getByText('This survey is not current in the live environment, and publishing will add it.'))
      .toBeInTheDocument()
  })
})
