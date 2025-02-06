import React from 'react'

import { mockEnrollee, mockStudyEnvContext } from 'test-utils/mocking-utils'
import { screen, waitFor } from '@testing-library/react'
import { renderWithRouter } from '@juniper/ui-core'
import Api from 'api/api'
import {
  mockParticipantUser,
  mockPortalParticipantUser
} from '@juniper/ui-participant/src/test-utils/test-participant-factory'
import EnrolleeOverview from './EnrolleeOverview'

test('renders enrollee participantUser info', async () => {
  jest.spyOn(Api, 'findRelationsByShortcode').mockResolvedValue([])
  jest.spyOn(Api, 'fetchParticipantUser').mockResolvedValue({
    ...mockParticipantUser(),
    username: 'someone',
    portalParticipantUsers: [{
      ...mockPortalParticipantUser(),
      lastLogin: 1626825600000
    }]
  })

  renderWithRouter(<EnrolleeOverview
    enrollee={mockEnrollee()}
    studyEnvContext={mockStudyEnvContext()} onUpdate={jest.fn()}/>)

  await waitFor(() => expect(screen.getByText('someone')).toBeInTheDocument())
})
