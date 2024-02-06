import React from 'react'
import {
  mockConfiguredSurvey,
  mockParticipantTask,
  mockStudyEnvParams,
  mockSurvey
} from 'test-utils/mocking-utils'
import { render, screen, waitFor } from '@testing-library/react'
import Api from 'api/api'
import SurveyEnvironmentDetailModal from './SurveyEnvironmentDetailModal'

describe('SurveyEnvironmentDetailModal', () => {
  test('enables updating of participant versions', async () => {
    jest.spyOn(Api, 'findConfiguredSurveys').mockImplementation(() => Promise.resolve(
      [{
        ...mockConfiguredSurvey(),
        survey: {
          ...mockSurvey(),
          version: 1
        }
      }, {
        ...mockConfiguredSurvey(),
        survey: {
          ...mockSurvey(),
          version: 2
        }
      }]
    ))
    jest.spyOn(Api, 'findTasksForStableId').mockImplementation(() => Promise.resolve(
      [{
        ...mockParticipantTask('SURVEY', 'COMPLETE'),
        targetAssignedVersion: 1
      }, {
        ...mockParticipantTask('SURVEY', 'COMPLETE'),
        targetAssignedVersion: 1
      }, {
        ...mockParticipantTask('SURVEY', 'COMPLETE'),
        targetAssignedVersion: 2
      }]
    ))

    render(<SurveyEnvironmentDetailModal studyEnvParams={mockStudyEnvParams()}
      stableId="survey1" onDismiss={jest.fn()} />)

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    // button should appear since there are participants assigned to version 1
    expect(screen.getByText('Update all to version 2')).toBeInTheDocument()
  })
})
