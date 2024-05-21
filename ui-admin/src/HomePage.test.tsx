import { render, screen } from '@testing-library/react'
import React from 'react'
import HomePage from './HomePage'
import { makeMockPortal, makeMockPortalStudy } from './test-utils/mocking-utils'
import { useNavContext } from './navbar/NavContextProvider'
import { setupRouterTest } from './test-utils/router-testing-utils'

jest.mock('./navbar/NavContextProvider')

describe('HomePage', () => {
  it('renders the study list in alphabetical order', () => {
    const portalList = [
      makeMockPortal('Z Portal', [
        makeMockPortalStudy('Z Study', 'studyZ'),
        makeMockPortalStudy('A Study', 'studyA'),
        makeMockPortalStudy('M Study 1', 'studyM1')
      ], 'studyZ shortcode'),
      makeMockPortal('A Portal', [
        makeMockPortalStudy('M Study 2', 'studyM2')
      ], 'studyA shortcode')
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

    expect(studies[0]).toHaveTextContent('M Study 2') // the study from "A Portal"
    expect(studies[1]).toHaveTextContent('A Study') // the remaining three studies from "Z Portal"
    expect(studies[2]).toHaveTextContent('M Study 1')
    expect(studies[3]).toHaveTextContent('Z Study')
  })
})
