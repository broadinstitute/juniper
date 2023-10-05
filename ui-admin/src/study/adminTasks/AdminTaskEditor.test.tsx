import React from 'react'
import { mockAdminTask, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import { mockAdminUser } from 'test-utils/user-mocking-utils'
import { select } from 'react-select-event'
import { AdminTaskEditModal } from './AdminTaskEditor'
import userEvent from '@testing-library/user-event'
import Api from 'api/api'


test('can update a task', async () => {
  const apiUpdateSpy = jest.spyOn(Api, 'updateAdminTask').mockImplementation(jest.fn())
  const studyEnvContext = mockStudyEnvContext()
  const users = [mockAdminUser(false)]
  const task = mockAdminTask()
  render(<AdminTaskEditModal task={task} studyEnvContext={studyEnvContext} users={users} onDismiss={jest.fn()}/>)
  expect(screen.getByText('Update admin task')).toBeInTheDocument()
  select(screen.getByLabelText('Status'), 'COMPLETE')
  await userEvent.type(screen.getByLabelText('Note'), 'did it')
  await userEvent.click(screen.getByText('Save'))

  expect(apiUpdateSpy).toHaveBeenCalledWith('portalCode', 'fakeStudy', 'sandbox', {
    'assignedAdminUserId': task.assignedAdminUserId,
    'createdAt': 0,
    'creatingAdminUserId': task.creatingAdminUserId,
    'dispositionNote': 'did it',
    'id': task.id,
    'status': 'COMPLETE',
    'studyEnvironmentId': task.studyEnvironmentId
  }
  )
})
