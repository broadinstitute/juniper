import React from 'react'
import { render, screen } from '@testing-library/react'
import { mockAssignedKitRequest, mockKitRequest } from 'test-utils/test-participant-factory'
import KitBanner from './KitBanner'
import { instantToDateString } from '../../util/timeUtils'
import { MockI18nProvider, setupRouterTest } from '@juniper/ui-core'

describe('HubPageKits', () => {
  it('renders a sent kit banner', () => {
    const sentKitRequest = mockKitRequest('SENT', 'SALIVA')
    const sentDate = instantToDateString(sentKitRequest.sentAt)
    const kitRequests = [
      sentKitRequest,
      mockKitRequest('RECEIVED', 'BLOOD')
    ]

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <KitBanner kitRequests={kitRequests}/>
      </MockI18nProvider>)
    render(RoutedComponent)
    // with two kits, one sent and one received, we should only see the sent kit
    expect(screen.getByText('{kitsPageTitle}')).toBeInTheDocument()
    expect(screen.getByText(sentDate)).toBeInTheDocument()
    expect(screen.getByText('Your saliva kit is on its way.', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('A sample kit was shipped')).toBeInTheDocument()
    expect(screen.queryByText('Your blood kit is on its way.')).not.toBeInTheDocument()
  })

  it('renders a kit provided banner', () => {
    const mockKitRequest = mockAssignedKitRequest('CREATED', 'SALIVA')
    const kitRequests = [mockKitRequest]

    const sentDate = instantToDateString(mockKitRequest.createdAt)

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <KitBanner kitRequests={kitRequests}/>
      </MockI18nProvider>)
    render(RoutedComponent)

    expect(screen.getByText('{kitsPageTitle}')).toBeInTheDocument()
    expect(screen.getByText(sentDate)).toBeInTheDocument()
    expect(screen.getByText('You have received a sample kit')).toBeInTheDocument()
  })

  it('renders a kit collected banner', () => {
    const mockKitRequest = mockAssignedKitRequest('COLLECTED_BY_STAFF', 'SALIVA')
    const kitRequests = [mockKitRequest]

    const sentDate = instantToDateString(mockKitRequest.createdAt)

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <KitBanner kitRequests={kitRequests}/>
      </MockI18nProvider>
    )
    render(RoutedComponent)

    expect(screen.getByText('{kitsPageTitle}')).toBeInTheDocument()
    expect(screen.getByText(sentDate)).toBeInTheDocument()
    expect(screen.getByText('Your sample kit has been received')).toBeInTheDocument()
  })
})
