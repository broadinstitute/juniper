import { render, screen } from '@testing-library/react'
import React from 'react'
import HomePage from './HomePage'
import { mockPortal } from './test-utils/mocking-utils'
import { useNavContext } from './navbar/NavContextProvider'
import { setupRouterTest } from './test-utils/router-testing-utils'

jest.mock('./navbar/NavContextProvider')

describe('HomePage', () => {
  it('renders the study list in alphabetical order', () => {
    const portalList = [
      {
        ...mockPortal(),
        name: 'Z Portal',
        portalStudies: [
          {
            study: {
              name: 'Z Study',
              shortcode: 'studyZ'
            }
          },
          {
            study: {
              name: 'A Study',
              shortcode: 'studyA'
            }
          },
          {
            study: {
              name: 'M Study 1',
              shortcode: 'studyM1'
            }
          }
        ]
      },
      {
        ...mockPortal(),
        name: 'A Portal',
        portalStudies: [
          {
            study: {
              name: 'M Study 2',
              shortcode: 'studyM2'
            }
          }
        ]
      }
    ]

    const mockContextValue = {
      breadCrumbs: [],
      setBreadCrumbs: jest.fn(),
      portalList,
      setPortalList: jest.fn()
    }

    ;(useNavContext as jest.Mock).mockReturnValue(mockContextValue)

    const { RoutedComponent } = setupRouterTest(<HomePage/>)
    render(RoutedComponent)

    expect(screen.getByText('My Studies')).toBeInTheDocument()

    const studies = screen.queryAllByText(/Study/)

    expect(studies[0]).toHaveTextContent('M Study 2')
    expect(studies[1]).toHaveTextContent('A Study')
    expect(studies[2]).toHaveTextContent('M Study 1')
    expect(studies[3]).toHaveTextContent('Z Study')
  })
})
