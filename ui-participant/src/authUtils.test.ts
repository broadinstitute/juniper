import { UserManager } from 'oidc-client-ts'
import { changePasswordRedirect } from './authUtils'
import { mockB2cConfig, mockEnvSpec } from './test-utils/mocking-utils'

jest.mock('oidc-client-ts')

describe('changePasswordRedirect', () => {
  it('passes selectedLanguage as a query parameter to signinRedirect', () => {
    const mockSigninRedirect = jest.fn()
    // @ts-expect-error "TS doesn't know about mocks"
    UserManager.mockImplementation(() => {
      return {
        signinRedirect: mockSigninRedirect
      }
    })

    const selectedLanguage = 'en'
    const envSpec = mockEnvSpec()

    changePasswordRedirect(mockB2cConfig(), envSpec, selectedLanguage)

    expect(mockSigninRedirect).toHaveBeenCalledWith({
      redirectMethod: 'replace',
      extraQueryParams: {
        portalShortcode: envSpec.shortcode,
        // eslint-disable-next-line camelcase
        ui_locales: selectedLanguage
      }
    })
  })
})
