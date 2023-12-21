import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import { mockKitRequest } from 'test-utils/test-participant-factory'
import KitBanner from './KitBanner'
import { instantToDateString } from '../../util/timeUtils'

describe('HubPageKits', () => {
  it('renders a sent kit banner', () => {
    const sentKitRequest = mockKitRequest('SENT', 'SALIVA')
    const sentDate = instantToDateString(sentKitRequest.sentAt)
    const kitRequests = [
      sentKitRequest,
      mockKitRequest('RECEIVED', 'BLOOD')
    ]

    const { RoutedComponent } = setupRouterTest(<KitBanner kitRequests={kitRequests}/>)
    render(RoutedComponent)
    // with two kits, one sent and one received, we should only see the sent kit
    expect(screen.getByText('Sample collection kits')).toBeInTheDocument()
    expect(screen.getByText(sentDate)).toBeInTheDocument()
    expect(screen.getByText('Your saliva kit is on its way.')).toBeInTheDocument()
    expect(screen.getByText('A sample kit was shipped')).toBeInTheDocument()
    expect(screen.queryByText('Your blood kit is on its way.')).not.toBeInTheDocument()
  })
})
