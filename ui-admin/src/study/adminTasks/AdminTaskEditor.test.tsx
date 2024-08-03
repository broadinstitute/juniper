import React from 'react'
import { mockAdminTask, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import { mockAdminUser } from 'test-utils/user-mocking-utils'
import { select } from 'react-select-event'
import { AdminTaskEditModal } from './AdminTaskEditor'
import { userEvent } from '@testing-library/user-event'
import Api from 'api/api'


test('can update a task', async () => {
  const apiUpdateSpy = jest.spyOn(Api, 'updateAdminTask').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const users = [mockAdminUser(false)]
  const task = mockAdminTask()
  render(<AdminTaskEditModal task={task} studyEnvContext={studyEnvContext} users={users} onDismiss={jest.fn()}/>)
  expect(screen.getByText('Update admin task')).toBeInTheDocument()
  select(screen.getByLabelText('Status'), 'Complete')
  await userEvent.click(screen.getByText('Save'))

  expect(apiUpdateSpy).toHaveBeenCalledWith('portalCode', 'fakeStudy', 'sandbox', {
    'assignedAdminUserId': task.assignedAdminUserId,
    'createdAt': 0,
    'blocksHub': false,
    'taskOrder': 1,
    'enrolleeId': 'enrolleeId1',
    'portalParticipantUserId': 'ppUserId1',
    'creatingAdminUserId': task.creatingAdminUserId,
    'id': task.id,
    'taskType': 'ADMIN_FORM',
    'status': 'COMPLETE',
    'studyEnvironmentId': task.studyEnvironmentId
  }
  )
})
