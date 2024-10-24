import { ParticipantTask } from 'api/api'

import { Enrollee } from '@juniper/ui-core'
import { isTaskAccessible } from './task/taskUtils'

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
