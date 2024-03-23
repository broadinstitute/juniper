/**
 * Returns a mock object that can be returned by the useUser hook
 */
export const mockUseUser = (isAnonymous: boolean) => {
  return {
    user: { isAnonymous, token: '', username: isAnonymous ? 'anonymous' : 'testUser' },
    logoutUser: jest.fn(),
    updateProfile: jest.fn(),
    updateEnrollee: jest.fn(),
    loginUserInternal: jest.fn(),
    loginUser: jest.fn(),
    enrollees: []
  }
}
