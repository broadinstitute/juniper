import { mockStudyEnvContext, renderInPortalRouter } from 'test-utils/mocking-utils'
import Api, { ParticipantUserMerge } from 'api/api'
import React from 'react'
import { waitFor, screen } from '@testing-library/react'
import ParticipantMergeView from './ParticipantMergeView'

test('renders a one-sided enrollee merge', async () => {
  const mergePlan: ParticipantUserMerge = {
    'users': {
      'pair': {
        'source': {
          'id': '05726fff-16d5-49cd-bac7-d09f1c3ca75d',
          'createdAt': 0,
          'username': 'someone@gmail.com',
          'shortcode': 'ACC_SOME',
          lastLogin: 0,
          token: '1'
        },
        'target': {
          'id': 'bfd74b55-8730-444f-9904-7ffa9436c6cd',
          'createdAt': 0,
          'username': 'SOMEONE@gmail.com',
          'shortcode': 'ACC_OTHER',
          lastLogin: 0,
          token: '1'
        }
      },
      'action': 'DELETE_SOURCE'
    },
    'ppUsers': {
      'pair': {
        'source': {
          'id': '7c1472bd-f069-4ca3-98ee-fe95513ac675',
          'createdAt': 0,
          'lastLogin': 0,
          'profile': {
            'id': 'd1d3d01b-5ec7-49f4-9946-27669560042f',
            'givenName': 'somebody',
            'familyName': 'fam',
            'preferredLanguage': 'en',
            'contactEmail': 'gavredhead@gmail.com',
            'doNotEmail': false,
            'doNotEmailSolicit': false,
            'birthDate': [
              2000,
              1,
              1
            ]
          }
        },
        'target': {
          'id': 'cf7207b8-9cef-48f1-b289-bf7dcedb08a8',
          'createdAt': 0,
          'profile': {
            'id': '4ca732af-061d-4c85-94f9-75877457862d',
            'preferredLanguage': '',
            'contactEmail': 'Gavredhead@gmail.com',
            'doNotEmail': false,
            'doNotEmailSolicit': false
          },
          'lastLogin': 0
        }
      },
      'action': 'DELETE_SOURCE'
    },
    'enrollees': [
      {
        'pair': {
          'source': {
            'id': 'bfa9fc3b-a9fc-4b2f-977b-06044905344f',
            'createdAt': 0,
            'lastUpdatedAt': 0,
            'participantUserId': '05726fff-16d5-49cd-bac7-d09f1c3ca75d',
            'profileId': 'd1d3d01b-5ec7-49f4-9946-27669560042f',
            'studyEnvironmentId': '745f7f60-c201-402f-817e-d3917f44b024',
            'shortcode': 'ENCODE',
            'subject': true,
            'consented': false,
            'familyEnrollees': [],
            'surveyResponses': [],
            'participantTasks': [],
            'participantNotes': [],
            'kitRequests': [],
            'relations': [],
            profile: {}
          }
        },
        'action': 'MOVE_SOURCE'
      }
    ]
  }
  jest.spyOn(Api, 'fetchMergePlan').mockResolvedValue(mergePlan)
  const studyEnvContext = mockStudyEnvContext()
  renderInPortalRouter(studyEnvContext.portal,
    <ParticipantMergeView studyEnvContext={studyEnvContext} source={'some@test.com'}
      target={'dupe@test.com'} onUpdate={jest.fn()}/>)

  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())

  expect(screen.getByText('ENCODE')).toBeInTheDocument()
})
