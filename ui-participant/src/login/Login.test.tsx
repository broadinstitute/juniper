import React from 'react'
import { render } from '@testing-library/react'
import { useAuth } from 'react-oidc-context'
import Login from './Login'
import { MockI18nProvider } from '@juniper/ui-core'
import { setupRouterTest } from '../test-utils/router-testing-utils'

jest.mock('react-oidc-context')

describe('Login', () => {
  it('calls signinRedirect with the correct ui_locales param', () => {
    const mockSigninRedirect = jest.fn()

    ;(useAuth as jest.Mock).mockReturnValue({
      signinRedirect: mockSigninRedirect
    })

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider selectedLanguage={'dev'}>
        <Login />
      </MockI18nProvider>
    )
    render(RoutedComponent)

    expect(mockSigninRedirect).toHaveBeenCalledWith({
      redirectMethod: 'replace',
      extraQueryParams: {
        portalShortcode: undefined,
        // eslint-disable-next-line camelcase
        ui_locales: 'dev'
      }
    })
  })
})
