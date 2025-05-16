import {
  getNextConsentTask,
  isTaskAccessible,
  taskComparator
} from './taskUtils'
import { shuffle } from 'lodash'
import {
  mockEnrollee,
  mockHubResponse,
  mockParticipantTask
} from '../test-utils/mocking-utils'

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

describe('isTaskAccessible', () => {
  it('handles no required tasks', () => {
    const enrollee = mockEnrollee()

    const task1 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: false,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    const task2 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: false,
      targetStableId: 'survey2',
      taskOrder: 2
    }

    enrollee.participantTasks = shuffle([task1, task2])

    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task2, enrollee)).toBe(true)

    const consentTask = {
      ...mockParticipantTask('CONSENT', 'NEW'),
      blocksHub: false,
      targetStableId: 'consent1',
      taskOrder: 0
    }

    enrollee.participantTasks = shuffle([task1, task2, consentTask])
    expect(isTaskAccessible(consentTask, enrollee)).toBe(true)
    expect(isTaskAccessible(task1, enrollee)).toBe(false)
    expect(isTaskAccessible(task2, enrollee)).toBe(false)


    const completeConsentTask = {
      ...mockParticipantTask('CONSENT', 'COMPLETE'),
      blocksHub: false,
      targetStableId: 'consent1',
      taskOrder: 0
    }

    enrollee.participantTasks = shuffle([task1, task2, completeConsentTask])

    expect(isTaskAccessible(completeConsentTask, enrollee)).toBe(true)
    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task2, enrollee)).toBe(true)
  })

  it('handles basic non-longitudinal required tasks', () => {
    const enrollee = mockEnrollee()
    const task1 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }
    const task2 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey2',
      taskOrder: 2
    }
    const task3 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey3',
      taskOrder: 3
    }
    const task4 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: false,
      targetStableId: 'survey4',
      taskOrder: 0
    }

    enrollee.participantTasks = shuffle([task1, task2, task3, task4]) // shuffle to simulate random order

    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task2, enrollee)).toBe(false)
    expect(isTaskAccessible(task3, enrollee)).toBe(false)
    expect(isTaskAccessible(task4, enrollee)).toBe(false) // required surveys must be completed, even if not in order

    const task1Completed = {
      ...mockParticipantTask('SURVEY', 'COMPLETE'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    enrollee.participantTasks = shuffle([task1Completed, task2, task3, task4])

    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task2, enrollee)).toBe(true)
    expect(isTaskAccessible(task3, enrollee)).toBe(false)
    expect(isTaskAccessible(task4, enrollee)).toBe(false) // required surveys must be completed, even if not in order

    const task2Completed = {
      ...mockParticipantTask('SURVEY', 'COMPLETE'),
      blocksHub: true,
      targetStableId: 'survey2',
      taskOrder: 2
    }

    const task3Completed = {
      ...mockParticipantTask('SURVEY', 'COMPLETE'),
      blocksHub: true,
      targetStableId: 'survey3',
      taskOrder: 3
    }

    enrollee.participantTasks = shuffle([task1Completed, task2Completed, task3Completed, task4])

    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task2, enrollee)).toBe(true)
    expect(isTaskAccessible(task3, enrollee)).toBe(true)
    expect(isTaskAccessible(task4, enrollee)).toBe(true) // all required surveys now complete


    const consentTask = {
      ...mockParticipantTask('CONSENT', 'NEW'),
      blocksHub: false,
      targetStableId: 'consent1',
      taskOrder: 0
    }

    enrollee.participantTasks = shuffle([task1Completed, task2Completed, task3Completed, task4, consentTask])

    // regardless of other tasks, if consent not complete, no other tasks are accessible
    expect(isTaskAccessible(consentTask, enrollee)).toBe(true) // consent tasks are always accessible
    expect(isTaskAccessible(task1, enrollee)).toBe(false)
    expect(isTaskAccessible(task2, enrollee)).toBe(false)
    expect(isTaskAccessible(task3, enrollee)).toBe(false)
    expect(isTaskAccessible(task4, enrollee)).toBe(false)

    const completeConsentTask = {
      ...mockParticipantTask('CONSENT', 'COMPLETE'),
      blocksHub: false,
      targetStableId: 'consent1',
      taskOrder: 0
    }

    enrollee.participantTasks = shuffle([task1Completed, task2Completed, task3Completed, task4, completeConsentTask])

    expect(isTaskAccessible(completeConsentTask, enrollee)).toBe(true)
    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task2, enrollee)).toBe(true)
    expect(isTaskAccessible(task3, enrollee)).toBe(true)
    expect(isTaskAccessible(task4, enrollee)).toBe(true)
  })

  it('handles removed surveys', () => {
    const enrollee = mockEnrollee()
    const task1 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }
    const task2Removed = {
      ...mockParticipantTask('SURVEY', 'REMOVED'),
      blocksHub: true,
      targetStableId: 'survey2',
      taskOrder: 2
    }
    const task3 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey3',
      taskOrder: 3
    }
    const task4 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: false,
      targetStableId: 'survey4',
      taskOrder: 0
    }

    enrollee.participantTasks = shuffle([task1, task2Removed, task3, task4])

    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task3, enrollee)).toBe(false)
    expect(isTaskAccessible(task4, enrollee)).toBe(false)

    const task1Completed = {
      ...mockParticipantTask('SURVEY', 'COMPLETE'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    enrollee.participantTasks = shuffle([task1Completed, task2Removed, task3, task4])

    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task3, enrollee)).toBe(true)
    expect(isTaskAccessible(task4, enrollee)).toBe(false)

    const task2New = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey2',
      taskOrder: 2
    }

    enrollee.participantTasks = shuffle([task1Completed, task2Removed, task2New, task3, task4])

    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task2New, enrollee)).toBe(true)
    expect(isTaskAccessible(task3, enrollee)).toBe(false)
    expect(isTaskAccessible(task4, enrollee)).toBe(false)
  })

  it('handles longitudinal surveys', () => {
    const enrollee = mockEnrollee()

    const task1New = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    const task1InProgress = {
      ...mockParticipantTask('SURVEY', 'IN_PROGRESS'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    const task2New = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey2',
      taskOrder: 2
    }

    const task3New = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey3',
      taskOrder: 3
    }

    enrollee.participantTasks = shuffle([task1New, task1InProgress, task2New, task3New])

    expect(isTaskAccessible(task1New, enrollee)).toBe(true)
    expect(isTaskAccessible(task1InProgress, enrollee)).toBe(true)
    expect(isTaskAccessible(task2New, enrollee)).toBe(false)
    expect(isTaskAccessible(task3New, enrollee)).toBe(false)

    const task1Completed = {
      ...mockParticipantTask('SURVEY', 'COMPLETE'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    enrollee.participantTasks = shuffle([task1New, task1Completed, task1InProgress, task2New, task3New])

    expect(isTaskAccessible(task1New, enrollee)).toBe(true)
    expect(isTaskAccessible(task1InProgress, enrollee)).toBe(true)
    expect(isTaskAccessible(task1Completed, enrollee)).toBe(true)
    // since task1 has any completed response, task2 is accessible even though there's a new task
    expect(isTaskAccessible(task2New, enrollee)).toBe(true)
    expect(isTaskAccessible(task3New, enrollee)).toBe(false)

    const task1Removed = {
      ...mockParticipantTask('SURVEY', 'REMOVED'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    enrollee.participantTasks = shuffle([task1New, task1Removed, task1Completed, task1InProgress, task2New, task3New])

    expect(isTaskAccessible(task1New, enrollee)).toBe(true)
    expect(isTaskAccessible(task1InProgress, enrollee)).toBe(true)
    expect(isTaskAccessible(task1Completed, enrollee)).toBe(true)
    expect(isTaskAccessible(task2New, enrollee)).toBe(true) // doesn't matter if there's removed either
  })

  it('handles task order issues', () => {
    const enrollee = mockEnrollee()

    const task1 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    const task2 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey2',
      taskOrder: 1
    }

    const task3 = {
      ...mockParticipantTask('SURVEY', 'NEW'),
      blocksHub: true,
      targetStableId: 'survey3',
      taskOrder: 1
    }

    enrollee.participantTasks = shuffle([task1, task2, task3])

    // even though they all have taskOrder1, sorting
    // should be stable by taskStableId
    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task2, enrollee)).toBe(false)
    expect(isTaskAccessible(task3, enrollee)).toBe(false)

    const task1Completed = {
      ...mockParticipantTask('SURVEY', 'COMPLETE'),
      blocksHub: true,
      targetStableId: 'survey1',
      taskOrder: 1
    }

    enrollee.participantTasks = shuffle([task1, task1Completed, task2, task3])

    expect(isTaskAccessible(task1, enrollee)).toBe(true)
    expect(isTaskAccessible(task1Completed, enrollee)).toBe(true)
    expect(isTaskAccessible(task2, enrollee)).toBe(true)
    expect(isTaskAccessible(task3, enrollee)).toBe(false)
  })
})
