import { render, screen } from '@testing-library/react'
import React from 'react'

import {
  HtmlPage,
  NavbarItemExternal,
  NavbarItemInternal,
  NavbarItemInternalAnchor,
  NavbarItemMailingList, PortalStudy
} from 'api/api'
import { setupRouterTest } from 'test-utils/router-testing-utils'

import { CustomNavLink, getMainJoinLink, LanguageDropdown } from './Navbar'

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
    render(<CustomNavLink navLink={navbarItem} />)

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
    const joinPath = getMainJoinLink(portalStudies)
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
    const joinPath = getMainJoinLink(portalStudies)
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
    const joinPath = getMainJoinLink(portalStudies)
    expect(joinPath).toBe('/studies/foo/join')
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
