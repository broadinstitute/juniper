import { mockEnrollee, mockPortal, mockStudyEnvContext, renderInPortalRouter } from 'test-utils/mocking-utils'
import { mockParticipantUser } from '@juniper/ui-participant/src/test-utils/test-participant-factory'
import PortalUserList from './PortalUserList'
import { mockStudy, mockStudyEnv } from '@juniper/ui-participant/src/test-utils/test-portal-factory'
import Api, { ParticipantUsersAndEnrollees, Study } from 'api/api'
import React from 'react'
import { screen, waitFor } from '@testing-library/react'
import { expectCellToHaveText } from '../../../test-utils/table-testing-utils'
import { userEvent } from '@testing-library/user-event'

// confirm it can show a user list, identify duplicates, and preview a merge
test('shows participantUser list', async () => {
  const studies: Study[] = [
    {
      ...mockStudy(),
      shortcode: 'study1',
      studyEnvironments: [{
        ...mockStudyEnv(),
        id: 'studyEnv1'
      }, {
        ...mockStudyEnv(),
        id: 'studyEnv1'
      }]
    },
    {
      ...mockStudy(),
      shortcode: 'study2',
      studyEnvironments: [{
        ...mockStudyEnv(),
        id: 'studyEnv2'
      }, {
        ...mockStudyEnv(),
        id: 'studyEnv2'
      }]
    }
  ]
  const users: ParticipantUsersAndEnrollees = {
    participantUsers: [{
      ...mockParticipantUser(),
      id: 'id1',
      username: 'user1'
    }, {
      ...mockParticipantUser(),
      id: 'id2',
      username: 'User2'
    }, {
      ...mockParticipantUser(),
      id: 'id3',
      username: 'user3'
    }],
    enrollees: [{
      ...mockEnrollee(),
      profile: { givenName: 'John', familyName: 'Doe' },
      participantUserId: 'id1',
      shortcode: 'AAAAAA',
      studyEnvironmentId: 'studyEnv1'
    }, {
      ...mockEnrollee(),
      profile: { givenName: 'John', familyName: 'Doe' },
      participantUserId: 'id3',
      shortcode: 'CCCCCCC',
      studyEnvironmentId: 'studyEnv2'
    }]
  }
  jest.spyOn(Api, 'fetchStudiesWithEnvs').mockResolvedValue(studies)
  jest.spyOn(Api, 'fetchParticipantUsers').mockResolvedValue(users)
  renderInPortalRouter(mockPortal(), <PortalUserList studyEnvContext={mockStudyEnvContext()}/>)
  await waitFor(() => expect(screen.queryByText('user1')).toBeInTheDocument())
  expect(screen.getByText(studies[0].shortcode)).toBeInTheDocument()
  expect(screen.getByText(studies[1].shortcode)).toBeInTheDocument()
  // the table should have a column for each study, with enrollee shortcodes in it
  const table = screen.getByRole('table') as HTMLTableElement
  expectCellToHaveText(table, 'user1', studies[0].shortcode, `${users.enrollees[0].shortcode } ()`)
  expectCellToHaveText(table, 'user3', studies[1].shortcode, `${users.enrollees[1].shortcode } ()`)

  // user1 and user3 have enrollees with the same name
  await userEvent.click(screen.getByText('Possible Duplicates (1)'))
  expect(screen.getByText('user1')).toBeInTheDocument()
  expect(screen.getByText('user3')).toBeInTheDocument()
  expect(screen.queryByText('user2')).not.toBeInTheDocument()
  expect(screen.queryAllByText('John Doe')).toHaveLength(2)

  await userEvent.click(screen.getByText('Preview Merge'))
  expect(screen.getByLabelText('Duplicate email')).toHaveValue('user1')
  expect(screen.getByLabelText('Original email')).toHaveValue('user3')
})
