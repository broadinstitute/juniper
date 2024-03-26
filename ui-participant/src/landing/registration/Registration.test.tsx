import React from 'react'
import { render } from '@testing-library/react'
import { useAuth } from 'react-oidc-context'
import { MockI18nProvider } from '@juniper/ui-core'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import Registration from './Registration'

jest.mock('react-oidc-context')

describe('Registration', () => {
  it('calls signinRedirect with the correct ui_locales param and signup option', () => {
    const mockSigninRedirect = jest.fn()

    ;(useAuth as jest.Mock).mockReturnValue({
      signinRedirect: mockSigninRedirect
    })

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider selectedLanguage={'dev'}>
        <Registration />
      </MockI18nProvider>
    )
    render(RoutedComponent)

    expect(mockSigninRedirect).toHaveBeenCalledWith({
      redirectMethod: 'replace',
      extraQueryParams: {
        option: 'signup',
        // eslint-disable-next-line camelcase
        ui_locales: 'dev'
      }
    })
  })
})
