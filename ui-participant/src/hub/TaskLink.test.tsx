import { ParticipantTask } from 'api/api'

import {
  Enrollee,
  isTaskAccessible,
  MockI18nProvider,
  renderWithRouter
} from '@juniper/ui-core'
import {
  mockEnrollee,
  mockParticipantTask
} from '../test-utils/test-participant-factory'
import React from 'react'
import TaskLink from './TaskLink'
import { screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'

describe('isTaskAccessible', () => {
  it('returns true for completed tasks when another task blocks hub', () => {
    const enrollee = {
      participantTasks: [
        {
          status: 'COMPLETE',
          taskType: 'SURVEY',
          taskOrder: 0,
          blocksHub: true
        } as ParticipantTask,
        {
          status: 'NEW',
          taskType: 'SURVEY',
          taskOrder: 1,
          blocksHub: true
        } as ParticipantTask
      ]
    } as Enrollee

    expect(isTaskAccessible(enrollee.participantTasks[0], enrollee)).toBe(true)
  })
})

describe('task display', () => {
  it('renders task history on click', async () => {
    const task = {
      ...mockParticipantTask('SURVEY', 'COMPLETE'),
      createdAt: 5000
    }
    const historicalTasks = [
      {
        ...mockParticipantTask('SURVEY', 'COMPLETE'),
        createdAt: 3000
      }, {
        ...mockParticipantTask('SURVEY', 'COMPLETE'),
        createdAt: 1000
      }
    ]
    renderWithRouter(
      <MockI18nProvider>
        <TaskLink task={task} studyShortcode="AAABBB"
          enrollee={mockEnrollee()} history={historicalTasks} />
      </MockI18nProvider>)
    const expectedLinkText = `{${task.targetStableId}:${task.targetAssignedVersion}}`
    expect(screen.queryAllByText(expectedLinkText)).toHaveLength(1)
    await userEvent.click(screen.getByTitle('{taskHistory}'))
    expect(screen.queryAllByText(expectedLinkText)).toHaveLength(3)
  })
})
