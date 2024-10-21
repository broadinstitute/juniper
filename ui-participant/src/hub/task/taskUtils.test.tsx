import { getNextConsentTask, taskComparator } from './taskUtils'
import { mockEnrollee, mockHubResponse, mockParticipantTask } from 'test-utils/test-participant-factory'

describe('taskComparator', () => {
  it('should sort tasks by type', () => {
    const taskA = mockParticipantTask('CONSENT', 'NEW')
    const taskB = mockParticipantTask('SURVEY', 'NEW')
    expect(taskComparator(taskA, taskB)).toBeLessThan(0)
    expect(taskComparator(taskB, taskA)).toBeGreaterThan(0)
  })

  it('should sort tasks by status when types are the same', () => {
    const taskA = mockParticipantTask('SURVEY', 'IN_PROGRESS')
    const taskB = mockParticipantTask('SURVEY', 'NEW')
    expect(taskComparator(taskA, taskB)).toBeLessThan(0)
    expect(taskComparator(taskB, taskA)).toBeGreaterThan(0)
  })

  it('should sort tasks by task order when types and statuses are the same', () => {
    const taskA = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      taskOrder: 1
    }
    const taskB = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      taskOrder: 2
    }

    expect(taskComparator(taskA, taskB)).toBeLessThan(0)
    expect(taskComparator(taskB, taskA)).toBeGreaterThan(0)
  })

  it('should return 0 for tasks with the same type, status, and order', () => {
    const taskA = mockParticipantTask('SURVEY', 'NEW')
    const taskB = mockParticipantTask('SURVEY', 'NEW')
    expect(taskComparator(taskA, taskB)).toBe(0)
  })
})

describe('getNextConsentTask', () => {
  it('should return the next actionable consent task', () => {
    const hubResponse = {
      ...mockHubResponse(),
      enrollee: {
        ...mockEnrollee(),
        participantTasks: [
          {
            ...mockParticipantTask('SURVEY', 'NEW'),
            id: 'survey1',
            taskOrder: 3
          },
          {
            ...mockParticipantTask('CONSENT', 'NEW'),
            taskOrder: 1,
            id: 'consent1'
          },
          {
            ...mockParticipantTask('CONSENT', 'NEW'),
            taskOrder: 2,
            id: 'consent2'
          }
        ]
      }
    }
    const nextTask = getNextConsentTask(hubResponse)
    expect(nextTask?.id).toBe('consent1')
  })

  it('should return undefined if no consent tasks are available', () => {
    const hubResponse = {
      ...mockHubResponse(),
      enrollee: {
        ...mockEnrollee(),
        participantTasks: [mockParticipantTask('SURVEY', 'NEW')]
      }
    }
    const nextTask = getNextConsentTask(hubResponse)
    expect(nextTask).toBeUndefined()
  })

  it('should return undefined if all consent tasks are completed', () => {
    const hubResponse = {
      ...mockHubResponse(),
      enrollee: {
        ...mockEnrollee(),
        participantTasks: [
          {
            ...mockParticipantTask('SURVEY', 'COMPLETE'),
            id: 'survey1',
            taskOrder: 3
          },
          {
            ...mockParticipantTask('CONSENT', 'COMPLETE'),
            taskOrder: 1,
            id: 'consent1'
          },
          {
            ...mockParticipantTask('CONSENT', 'COMPLETE'),
            taskOrder: 2

          }
        ]
      }
    }
    const nextTask = getNextConsentTask(hubResponse)
    expect(nextTask).toBeUndefined()
  })
})
