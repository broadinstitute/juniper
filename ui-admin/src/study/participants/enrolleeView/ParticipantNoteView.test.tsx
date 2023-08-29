import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { mockEnrollee, mockParticipantNote } from 'test-utils/mocking-utils'
import { mockAdminUser } from 'test-utils/user-mocking-utils'
import { render, screen } from '@testing-library/react'
import { ParticipantNoteView } from './ParticipantNoteView'
import { ParticipantNote } from 'api/api'

test('renders a note with the admin user name', async () => {
  const enrollee = mockEnrollee()
  const users = [{
    ...mockAdminUser(false),
    id: 'fakeId2',
    username: 'user2'
  }, {
    ...mockAdminUser(false),
    id: 'fakeId3',
    username: 'user3 username'
  }]
  const note: ParticipantNote = {
    ...mockParticipantNote(),
    text: 'some note text',
    creatingAdminUserId: 'fakeId3',
    enrolleeId: enrollee.id
  }
  const { RoutedComponent } = setupRouterTest(
    <ParticipantNoteView enrollee={enrollee} currentEnvPath="path1" note={note} users={users}
    linkedTasks={[]}/>)
  render(RoutedComponent)
  expect(screen.getByText('user3 username')).toBeInTheDocument()
  expect(screen.getByText('some note text')).toBeInTheDocument()
})
