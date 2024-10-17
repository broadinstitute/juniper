import React from 'react'
import { render, screen } from '@testing-library/react'
import { asMockedFn, KitRequest, KitRequestStatus, MockI18nProvider, setupRouterTest } from '@juniper/ui-core'
import KitsPage from './KitsPage'
import { mockEnrollee, mockPortalParticipantUser, mockProfile } from 'test-utils/test-participant-factory'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { usePortalEnv } from 'providers/PortalProvider'
import { mockUsePortalEnv } from 'test-utils/test-portal-factory'

jest.mock('providers/PortalProvider', () => ({ usePortalEnv: jest.fn() }))

jest.mock('providers/ActiveUserProvider', () => ({
  useActiveUser: jest.fn()
}))

beforeEach(() => {
  asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
})

const mockActiveUserWithKits = (kitRequests: KitRequest[]) => {
  return {
    enrollees: [
      {
        ...mockEnrollee(),
        kitRequests
      }
    ],
    ppUser: mockPortalParticipantUser(),
    profile: mockProfile(),
    setActiveUser: jest.fn(),
    proxyRelations: [],
    updateProfile: jest.fn()
  }
}

const mailedKitStatuses: KitRequestStatus[] = [
  'NEW', 'CREATED', 'QUEUED', 'SENT', 'RECEIVED', 'ERRORED', 'DEACTIVATED', 'UNKNOWN'
]

const mailedKitStatusToTestIdMap: { [key in KitRequestStatus]: string[] } = {
  'NEW': ['preparing-true', 'shipped-false', 'returned-false'],
  'CREATED': ['preparing-true', 'shipped-false', 'returned-false'],
  'QUEUED': ['preparing-true', 'shipped-false', 'returned-false'],
  'SENT': ['preparing-true', 'shipped-true', 'returned-false'],
  'COLLECTED_BY_STAFF': [],
  'RECEIVED': ['preparing-true', 'shipped-true', 'returned-true'],
  'ERRORED': [],
  'DEACTIVATED': [],
  'UNKNOWN': []
}

const inPersonKitStatuses: KitRequestStatus[] = [
  'CREATED', 'COLLECTED_BY_STAFF', 'SENT', 'RECEIVED', 'ERRORED', 'DEACTIVATED', 'UNKNOWN'
]

const inPersonKitStatusToTestIdMap: { [key in KitRequestStatus]: string[] } = {
  'NEW': [],
  'CREATED': ['created-true', 'collected-false'],
  'QUEUED': [],
  'SENT': ['created-true', 'collected-false'],
  'COLLECTED_BY_STAFF': ['created-true', 'collected-true'],
  'RECEIVED': ['created-true', 'collected-true'],
  'ERRORED': [],
  'DEACTIVATED': [],
  'UNKNOWN': []
}

describe('KitsPage', () => {
  it('should render a message when there are no kits for an enrollee', () => {
    asMockedFn(useActiveUser).mockReturnValue(mockActiveUserWithKits([]))
    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><KitsPage/></MockI18nProvider>)
    render(RoutedComponent)
    expect(screen.getByText('{kitsPageTitle}')).toBeInTheDocument()
    expect(screen.getByText('{kitsPageYourKitsTitle} (0)')).toBeInTheDocument()
    expect(screen.getByText('{kitsPageNoKits}')).toBeInTheDocument()
  })

  mailedKitStatuses.forEach(status => {
    it(`should render correct progress bar for mailed kit with status: ${status}`, () => {
      asMockedFn(useActiveUser).mockReturnValue(mockActiveUserWithKits([
        {
          id: 'kit1',
          distributionMethod: 'MAILED',
          status,
          createdAt: 0,
          kitType: {
            id: 'blood',
            name: 'BLOOD',
            displayName: 'Blood',
            description: 'Blood kit'
          },
          sentToAddress: '123 Main St',
          skipAddressValidation: false
        }
      ]))
      const { RoutedComponent } = setupRouterTest(<MockI18nProvider><KitsPage/></MockI18nProvider>)
      render(RoutedComponent)
      expect(screen.getByText('{kitsPageTitle}')).toBeInTheDocument()

      const expectedTestIds = mailedKitStatusToTestIdMap[status]
      expectedTestIds.forEach(testId => {
        expect(screen.getByTestId(testId)).toBeInTheDocument()
      })
    })
  })

  inPersonKitStatuses.forEach(status => {
    it(`should render correct progress bar for in-person kit with status: ${status}`, () => {
      asMockedFn(useActiveUser).mockReturnValue(mockActiveUserWithKits([
        {
          id: 'kit1',
          distributionMethod: 'IN_PERSON',
          status,
          createdAt: 0,
          kitType: {
            id: 'saliva',
            name: 'SALIVA',
            displayName: 'Saliva',
            description: 'Saliva kit'
          },
          sentToAddress: '123 Main St',
          skipAddressValidation: false
        }
      ]))
      const { RoutedComponent } = setupRouterTest(<MockI18nProvider><KitsPage/></MockI18nProvider>)
      render(RoutedComponent)
      expect(screen.getByText('{kitsPageTitle}')).toBeInTheDocument()

      const expectedTestIds = inPersonKitStatusToTestIdMap[status]
      expectedTestIds.forEach(testId => {
        expect(screen.getByTestId(testId)).toBeInTheDocument()
      })
    })
  })
})
