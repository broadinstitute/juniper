import { UserContextT } from '../providers/UserProvider'

/**
 * Returns a mock object that can be returned by the useUser hook
 */
export const mockUseUser = (isAnonymous: boolean): UserContextT => {
  return {
    user: { id: '', isAnonymous, token: '', username: isAnonymous ? 'anonymous' : 'testUser' },
    logoutUser: jest.fn(),
    updateProfile: jest.fn(),
    updateEnrollee: jest.fn(),
    loginUserInternal: jest.fn(),
    loginUser: jest.fn(),
    refreshLogin: jest.fn(),
    ppUsers: [],
    enrollees: [],
    relations: []
  }
}
