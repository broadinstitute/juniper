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
import { userEvent } from '@testing-library/user-event'
import { Store } from 'react-notifications-component'

describe('SurveyEnvironmentDetailModal', () => {
  test('enables updating of participant versions', async () => {
    jest.spyOn(Api, 'findConfiguredSurveys').mockResolvedValue(
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
    )
    jest.spyOn(Api, 'findTasksForStableId').mockResolvedValue(
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
    )

    render(<SurveyEnvironmentDetailModal studyEnvParams={mockStudyEnvParams()}
      stableId="survey1" onDismiss={jest.fn()} />)

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    // button should appear since there are participants assigned to version 1
    expect(screen.getByText('Update all to version 2')).toBeInTheDocument()
  })

  test('assigns all unassigned enrollees on button', async () => {
    jest.spyOn(Api, 'findConfiguredSurveys').mockResolvedValue(
      [{
        ...mockConfiguredSurvey(),
        survey: {
          ...mockSurvey(),
          stableId: 'test1234',
          version: 2
        }
      }]
    )
    jest.spyOn(Store, 'addNotification').mockImplementation(jest.fn())
    jest.spyOn(Api, 'findTasksForStableId').mockResolvedValue([])
    const assignSpy = jest.spyOn(Api, 'assignParticipantTasksToEnrollees').mockResolvedValue([])

    render(<SurveyEnvironmentDetailModal studyEnvParams={mockStudyEnvParams()}
      stableId="test1234" onDismiss={jest.fn()} />)

    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())

    await userEvent.click(screen.getByText('Assign to sandbox participants'))
    expect(assignSpy).toHaveBeenCalledWith({
      envName: 'sandbox',
      portalShortcode: 'foo',
      studyShortcode: 'bar'
    }, {
      assignAllUnassigned: true,
      targetAssignedVersion: 2,
      targetStableId: 'test1234',
      taskType: 'SURVEY'
    })
  })
})
