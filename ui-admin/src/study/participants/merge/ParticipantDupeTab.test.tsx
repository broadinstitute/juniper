import { mockEnrollee } from 'test-utils/mocking-utils'
import { mockParticipantUser } from '@juniper/ui-participant/src/test-utils/test-participant-factory'
import { identifyDupes } from './UseParticipantDupeTab'
import { ParticipantUserWithEnrollees } from '../participantList/PortalUserList'

test('identifies username dupes', async () => {
  const users: ParticipantUserWithEnrollees[] = [{
    ...mockParticipantUser(),
    id: 'id1',
    username: 'user1',
    enrollees: [mockEnrollee()]
  }, {
    ...mockParticipantUser(),
    id: 'id2',
    username: 'User1',
    enrollees: []
  }, {
    ...mockParticipantUser(),
    id: 'id3',
    username: 'someoneElse',
    enrollees: []
  }]
  const dupes = identifyDupes(users)
  expect(dupes).toHaveLength(1)
  expect(dupes[0].dupeType).toEqual('username')
  expect(dupes[0].users).toHaveLength(2)
  expect(dupes[0].users.map(u => u.id)).toEqual(['id1', 'id2'])
})

test('identifies profile name dupes', async () => {
  const users: ParticipantUserWithEnrollees[] = [{
    ...mockParticipantUser(),
    id: 'id1',
    username: 'user1',
    enrollees: [{
      ...mockEnrollee(),
      profile: {
        givenName: 'John',
        familyName: 'Doe'
      }
    }]
  }, {
    ...mockParticipantUser(),
    id: 'id2',
    username: 'user2',
    enrollees: [{
      ...mockEnrollee(),
      profile: {
        givenName: 'Steve',
        familyName: 'Doe'
      }
    }]
  }, {
    ...mockParticipantUser(),
    id: 'id3',
    username: 'user3',
    enrollees: [{
      ...mockEnrollee(),
      profile: {
        givenName: 'john',
        familyName: 'Doe'
      }
    }]
  }]
  const dupes = identifyDupes(users)
  expect(dupes).toHaveLength(1)
  expect(dupes[0].dupeType).toEqual('name')
  expect(dupes[0].users).toHaveLength(2)
  expect(dupes[0].users.map(u => u.id)).toIncludeSameMembers(['id1', 'id3'])
})
