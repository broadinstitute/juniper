import {
  render,
  screen
} from '@testing-library/react'
import React from 'react'

import {
  HtmlPage,
  NavbarItemExternal,
  NavbarItemInternal,
  NavbarItemInternalAnchor,
  NavbarItemMailingList,
  PortalStudy
} from 'api/api'

import Navbar, {
  AccountOptionsDropdown,
  CustomNavLink,
  getMainJoinLink,
  LanguageDropdown
} from './Navbar'
import {
  asMockedFn,
  MockI18nProvider,
  setupRouterTest
} from '@juniper/ui-core'
import { UserManager } from 'oidc-client-ts'
import { useUser } from './providers/UserProvider'
import { usePortalEnv } from './providers/PortalProvider'
import {
  mockPortalEnvironmentConfig,
  mockUsePortalEnv
} from './test-utils/test-portal-factory'
import { mockUseUser } from './test-utils/user-mocking-utils'

jest.mock('oidc-client-ts')
jest.mock('providers/UserProvider')
jest.mock('mixpanel-browser')
jest.mock('providers/PortalProvider', () => {
  return {
    ...jest.requireActual('providers/PortalProvider'),
    usePortalEnv: jest.fn()
  }
})

describe('CustomNavLink', () => {
  it('renders internal links', () => {
    // Arrange
    const navbarItem: NavbarItemInternal = {
      id: 'foo',
      itemType: 'INTERNAL',
      text: 'Internal link',
      itemOrder: 1,
      htmlPage: {
        path: 'testPage'
      } as HtmlPage
    }

    const { RoutedComponent } = setupRouterTest(<CustomNavLink navLink={navbarItem} />)

    // Act
    render(RoutedComponent)

    // Assert
    const link = screen.getByText('Internal link')
    expect(link).toHaveAttribute('href', '/testPage')
  })

  it('renders internal anchor links', () => {
    // Arrange
    const navbarItem: NavbarItemInternalAnchor = {
      id: 'foo',
      itemType: 'INTERNAL_ANCHOR',
      text: 'Internal anchor link',
      itemOrder: 1,
      href: '/testPage#anchor'
    }

    const { RoutedComponent } = setupRouterTest(<CustomNavLink navLink={navbarItem} />)

    // Act
    render(RoutedComponent)

    // Assert
    const link = screen.getByText('Internal anchor link')
    expect(link).toHaveAttribute('href', '/testPage#anchor')
  })

  it('renders external links', () => {
    // Arrange
    const navbarItem: NavbarItemExternal = {
      id: 'foo',
      itemType: 'EXTERNAL',
      text: 'External link',
      itemOrder: 1,
      href: 'https://example.com'
    }

    // Act
    render(<CustomNavLink navLink={navbarItem} />)

    // Assert
    const link = screen.getByText('External link')
    expect(link).toHaveAttribute('href', 'https://example.com')
  })

  it('renders mailing list links', async () => {
    // Arrange
    const navbarItem: NavbarItemMailingList = {
      id: 'foo',
      itemType: 'MAILING_LIST',
      itemOrder: 1,
      text: 'Mailing list link'
    }

    // Act
    render(<MockI18nProvider><CustomNavLink navLink={navbarItem}/></MockI18nProvider>)

    // Assert
    const link = screen.getByText('Mailing list link')
    const modal = document.querySelector('.modal') as HTMLElement
    expect(link).toHaveAttribute('data-bs-toggle', 'modal')
    expect(link).toHaveAttribute('data-bs-target', `#${CSS.escape(modal.getAttribute('id') as string)}`)
  })
})

describe('joinPath', () => {
  it('joins the study if there is one study', () => {
    const portalStudies = [{
      study: {
        shortcode: 'foo',
        name: 'Foo',
        studyEnvironments: [{
          studyEnvironmentConfig: {
            acceptingEnrollment: true
          }
        }]
      }
    }] as PortalStudy[]
    const joinPath = getMainJoinLink(portalStudies, mockPortalEnvironmentConfig())
    expect(joinPath).toBe('/studies/foo/join')
  })

  it('joins the portal if there are two studies', () => {
    const portalStudies = [{
      study: {
        shortcode: 'foo',
        name: 'Foo',
        studyEnvironments: [{
          studyEnvironmentConfig: {
            acceptingEnrollment: true
          }
        }]
      }
    }, {
      study: {
        shortcode: 'bar',
        name: 'Bar',
        studyEnvironments: [{
          studyEnvironmentConfig: {
            acceptingEnrollment: true
          }
        }]
      }
    }] as PortalStudy[]
    const joinPath = getMainJoinLink(portalStudies, mockPortalEnvironmentConfig())
    expect(joinPath).toBe('/join')
  })

  it('joins the portal if only one study is accepting enrollment', () => {
    const portalStudies = [{
      study: {
        shortcode: 'foo',
        name: 'Foo',
        studyEnvironments: [{
          studyEnvironmentConfig: {
            acceptingEnrollment: true
          }
        }]
      }
    }, {
      study: {
        shortcode: 'bar',
        name: 'Bar',
        studyEnvironments: [{
          studyEnvironmentConfig: {
            acceptingEnrollment: false
          }
        }]
      }
    }] as PortalStudy[]
    const joinPath = getMainJoinLink(portalStudies, mockPortalEnvironmentConfig())
    expect(joinPath).toBe('/studies/foo/join')
  })

  it('joins the primary study if there are multiple', () => {
    const portalStudies = [{
      study: {
        shortcode: 'foo',
        name: 'Foo',
        studyEnvironments: [{
          studyEnvironmentConfig: {
            acceptingEnrollment: true
          }
        }]
      }
    }, {
      study: {
        shortcode: 'bar',
        name: 'Bar',
        studyEnvironments: [{
          studyEnvironmentConfig: {
            acceptingEnrollment: true
          }
        }]
      }
    }] as PortalStudy[]
    const joinPath = getMainJoinLink(portalStudies, {
      ...mockPortalEnvironmentConfig(), primaryStudy: 'bar'
    })
    expect(joinPath).toBe('/studies/bar/join')
  })
})

describe('Navbar', () => {
  it('renders an account dropdown when the user is logged in', async () => {
    asMockedFn(useUser).mockReturnValue(mockUseUser(false))
    asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())

    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><Navbar/></MockI18nProvider>)
    render(RoutedComponent)

    const accountDropdown = await screen.findByLabelText('account options for testUser')
    expect(accountDropdown).toBeInTheDocument()
  })

  it('does not render an account dropdown when the user is not logged in', () => {
    asMockedFn(useUser).mockReturnValue(mockUseUser(true))
    asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())

    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><Navbar/></MockI18nProvider>)
    render(RoutedComponent)

    const dropdownButton = screen.queryByLabelText('account options for anonymous')
    expect(dropdownButton).not.toBeInTheDocument()
  })
})

describe('AccountOptionsDropdown', () => {
  it('displays user account options when clicked', async () => {
    asMockedFn(useUser).mockReturnValue(mockUseUser(false))

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider>
        <AccountOptionsDropdown/>
      </MockI18nProvider>
    )

    render(RoutedComponent)

    const dropdownButton = screen.getByLabelText('account options for testUser')
    dropdownButton.click()

    const profileOption = screen.getByLabelText('edit profile')
    const changePasswordOption = screen.getByLabelText('change password')
    const logoutOption = screen.getByLabelText('log out')

    expect(profileOption).toBeInTheDocument()
    expect(changePasswordOption).toBeInTheDocument()
    expect(logoutOption).toBeInTheDocument()
  })

  it('changePassword calls signinRedirect with the correct ui_locales param', async () => {
    const mockSigninRedirect = jest.fn()
    jest.spyOn(UserManager.prototype, 'signinRedirect').mockImplementation(mockSigninRedirect)
    asMockedFn(useUser).mockReturnValue(mockUseUser(false))

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider selectedLanguage={'es'}>
        <AccountOptionsDropdown/>
      </MockI18nProvider>
    )

    render(RoutedComponent)

    const dropdownButton = screen.getByLabelText('account options for testUser')
    dropdownButton.click()
    const changePasswordOption = screen.getByLabelText('change password')
    changePasswordOption.click()

    expect(mockSigninRedirect).toHaveBeenCalledWith({
      redirectMethod: 'replace',
      extraQueryParams: {
        originUrl: 'http://localhost',
        portalEnvironment: 'live',
        portalShortcode: undefined,
        // eslint-disable-next-line camelcase
        ui_locales: 'es'
      }
    })
  })
})

describe('language selector', () => {
  it('renders a language selector', () => {
    const languageOptions = [
      { languageCode: 'en', languageName: 'English' },
      { languageCode: 'es', languageName: 'Spanish' }
    ]
    const selectedLanguage = 'en'
    const changeLanguage = jest.fn()
    const reloadPortal = jest.fn()

    const { RoutedComponent } = setupRouterTest(
      <LanguageDropdown
        languageOptions={languageOptions}
        selectedLanguage={selectedLanguage}
        changeLanguage={changeLanguage}
        reloadPortal={reloadPortal}
      />
    )
    render(RoutedComponent)

    const buttons = screen.queryAllByRole('button')
    expect(buttons[0]).toHaveTextContent('Select a language')
    expect(buttons[1]).toHaveTextContent('English')
    expect(buttons[2]).toHaveTextContent('Spanish')
  })

  it('reloads the portal when a language is selected', () => {
    const languageOptions = [
      { languageCode: 'en', languageName: 'English' },
      { languageCode: 'es', languageName: 'Spanish' }
    ]
    const selectedLanguage = 'en'
    const changeLanguage = jest.fn()
    const reloadPortal = jest.fn()

    const { RoutedComponent } = setupRouterTest(
      <LanguageDropdown
        languageOptions={languageOptions}
        selectedLanguage={selectedLanguage}
        changeLanguage={changeLanguage}
        reloadPortal={reloadPortal}
      />
    )
    render(RoutedComponent)

    const languageSelector = screen.getByLabelText('Select a language')
    languageSelector.click()
    const spanishButton = screen.getByText('Spanish')
    spanishButton.click()
    expect(changeLanguage).toHaveBeenCalledWith('es')
    expect(reloadPortal).toHaveBeenCalled()
  })

  it('does not render when there is only one language', () => {
    const languageOptions = [
      { languageCode: 'en', languageName: 'English' }
    ]
    const selectedLanguage = 'en'
    const changeLanguage = jest.fn()
    const reloadPortal = jest.fn()

    const { RoutedComponent } = setupRouterTest(
      <LanguageDropdown
        languageOptions={languageOptions}
        selectedLanguage={selectedLanguage}
        changeLanguage={changeLanguage}
        reloadPortal={reloadPortal}
      />
    )
    render(RoutedComponent)

    expect(screen.queryByLabelText('Select a language')).not.toBeInTheDocument()
  })
})
