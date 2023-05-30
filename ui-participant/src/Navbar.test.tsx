import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

import {
  HtmlPage,
  NavbarItemExternal,
  NavbarItemInternal,
  NavbarItemInternalAnchor,
  NavbarItemMailingList
} from 'api/api'
import { setupRouterTest } from 'test-utils/router-testing-utils'

import { CustomNavLink } from './Navbar'

describe('CustomNavLink', () => {
  it('renders internal links', () => {
    // Arrange
    const navbarItem: NavbarItemInternal = {
      itemType: 'INTERNAL',
      text: 'Internal link',
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
      itemType: 'INTERNAL_ANCHOR',
      text: 'Internal anchor link',
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
      itemType: 'EXTERNAL',
      text: 'External link',
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
    const user = userEvent.setup()

    const navbarItem: NavbarItemMailingList = {
      itemType: 'MAILING_LIST',
      text: 'Mailing list link'
    }

    jest.spyOn(window, 'alert').mockImplementation(() => { /* noop */ })

    // Act
    render(<CustomNavLink navLink={navbarItem} />)

    // Assert
    const link = screen.getByText('Mailing list link')

    // Act
    await user.click(link)

    // Assert
    expect(window.alert).toHaveBeenCalled()
  })
})
