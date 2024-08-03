import { render, screen } from '@testing-library/react'
import React from 'react'
import HomePage from './HomePage'
import { makeMockPortal } from './test-utils/mocking-utils'
import { useNavContext } from './navbar/NavContextProvider'
import { setupRouterTest } from '@juniper/ui-core'
import { userEvent } from '@testing-library/user-event'

jest.mock('./navbar/NavContextProvider')

describe('HomePage', () => {
  const mockPortalList = [
    makeMockPortal('E Portal', [], 'eshortcode'),
    makeMockPortal('A Portal', [], 'ashortcode'),
    makeMockPortal('D Portal', [], 'dshortcode'),
    makeMockPortal('C Portal', [], 'cshortcode'),
    makeMockPortal('B Portal', [], 'bshortcode')
  ]

  it('renders the portal list in alphabetical order', () => {
    const mockContextValue = {
      breadCrumbs: [],
      setBreadCrumbs: jest.fn(),
      portalList: mockPortalList,
      setPortalList: jest.fn()
    }

    ;(useNavContext as jest.Mock).mockReturnValue(mockContextValue)

    const { RoutedComponent } = setupRouterTest(<HomePage/>)
    render(RoutedComponent)

    expect(screen.getByText('Select a portal')).toBeInTheDocument()

    const studies = screen.queryAllByText(/Portal/)

    expect(studies[0]).toHaveTextContent('A Portal')
    expect(studies[1]).toHaveTextContent('B Portal')
    expect(studies[2]).toHaveTextContent('C Portal')
    expect(studies[3]).toHaveTextContent('D Portal')
    expect(studies[4]).toHaveTextContent('E Portal')
  })

  it('filters the portal list by search input', async () => {
    const mockContextValue = {
      breadCrumbs: [],
      setBreadCrumbs: jest.fn(),
      portalList: mockPortalList,
      setPortalList: jest.fn()
    }

    ;(useNavContext as jest.Mock).mockReturnValue(mockContextValue)

    const { RoutedComponent } = setupRouterTest(<HomePage/>)
    render(RoutedComponent)

    expect(screen.getByText('Select a portal')).toBeInTheDocument()

    await userEvent.type(screen.getByTitle('Search for portals or studies'), 'c port')
    await userEvent.click(screen.getByTitle('submit search'))

    const studies = screen.queryAllByText(/Portal/)

    expect(studies).toHaveLength(1)
    expect(studies[0]).toHaveTextContent('C Portal')
  })

  it('displays a warning message if the user does not have access to any portals', () => {
    const mockContextValue = {
      breadCrumbs: [],
      setBreadCrumbs: jest.fn(),
      portalList: [],
      setPortalList: jest.fn()
    }

    ;(useNavContext as jest.Mock).mockReturnValue(mockContextValue)

    const { RoutedComponent } = setupRouterTest(<HomePage/>)
    render(RoutedComponent)

    expect(screen.getByText('You do not have access to any portals or studies. ' +
        'If this is an error, please contact', { exact: false })).toBeInTheDocument()
  })

  it('toggles to a list view', async () => {
    const mockContextValue = {
      breadCrumbs: [],
      setBreadCrumbs: jest.fn(),
      portalList: mockPortalList,
      setPortalList: jest.fn()
    }

    ;(useNavContext as jest.Mock).mockReturnValue(mockContextValue)

    const { RoutedComponent } = setupRouterTest(<HomePage/>)
    render(RoutedComponent)

    const gridButton = screen.getByText('Grid')
    const listButton = screen.getByText('List')

    expect(gridButton).toHaveClass('btn-dark')
    expect(listButton).toHaveClass('btn-light')

    await userEvent.click(listButton)

    expect(gridButton).toHaveClass('btn-light')
    expect(listButton).toHaveClass('btn-dark')

    expect(screen.getByText('Portal Name')).toBeInTheDocument()
    expect(screen.getByText('Status')).toBeInTheDocument()
    expect(screen.getByText('Website')).toBeInTheDocument()
    expect(screen.getByText('Total Studies')).toBeInTheDocument()
    expect(screen.getByText('Created')).toBeInTheDocument()
  })
})
