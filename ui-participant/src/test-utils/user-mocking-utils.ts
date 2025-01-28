import { UserContextT } from 'providers/UserProvider'
import { ActiveUserContextT } from 'providers/ActiveUserProvider'
import { mockEnrollee } from './test-participant-factory'
import { mockProfile } from '@juniper/ui-core'

/**
 * Returns a mock object that can be returned by the useUser hook
 */
export const mockUseUser = (isAnonymous: boolean): UserContextT => {
  return {
    user: isAnonymous ? null : { id: '', token: '', username: isAnonymous ? 'anonymous' : 'testUser', lastLogin: 0 },
    logoutUser: jest.fn(),
    updateProfile: jest.fn(),
    updateEnrollee: jest.fn(),
    loginUserInternal: jest.fn(),
    loginUser: jest.fn(),
    refreshLoginState: jest.fn(),
    ppUsers: [],
    enrollees: [],
    proxyRelations: []
  }
}

/**
 Returns a mock object that can be returned by the useActiveUser hook
 */
export const mockUseActiveUser = (): ActiveUserContextT => {
  return {
    ppUser: {
      id: '',
      profile: mockProfile(),
      profileId: 'profile1',
      participantUserId: ''
    },
    profile: mockProfile(),
    enrollees: [mockEnrollee()],
    proxyRelations: [],
    setActiveUser: jest.fn(),
    updateProfile: jest.fn()
  }
}
