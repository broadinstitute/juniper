import React from 'react'

import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen, waitFor } from '@testing-library/react'
import { mockPortalContext, mockPortalEnvironment } from 'test-utils/mocking-utils'
import SiteImageList from './SiteImageList'
import Api from 'api/api'
import userEvent from '@testing-library/user-event'

test('renders empty table', async () => {
  jest.spyOn(Api, 'getPortalImages').mockImplementation(() => Promise.resolve([]))
  const { RoutedComponent } = setupRouterTest(
    <SiteImageList portalContext={mockPortalContext()}
      portalEnv={mockPortalEnvironment('sandbox')}/>)
  render(RoutedComponent)
  await waitFor(() => expect(screen.getByText('Showing 0 of 0 rows')).toBeInTheDocument())
})


test('renders table with a clickable image', async () => {
  jest.spyOn(Api, 'getPortalImages').mockImplementation(() => Promise.resolve([{
    id: 'fakeId',
    cleanFileName: 'testImage.png',
    version: 1,
    createdAt: Date.now()
  }]))
  const { RoutedComponent } = setupRouterTest(
    <SiteImageList portalContext={mockPortalContext()}
      portalEnv={mockPortalEnvironment('sandbox')}/>)
  render(RoutedComponent)
  await waitFor(() => expect(screen.getByText('Showing 1 of 1 rows')).toBeInTheDocument())
  expect(screen.getByText('testImage.png')).toBeInTheDocument()
  expect(screen.queryByAltText('full-size preview of testImage.png')).not.toBeInTheDocument()
  // clicking the image should popup the full-size preview
  await userEvent.click(screen.getByTitle('show full-size preview'))
  await waitFor(() => expect(screen.getByAltText('full-size preview of testImage.png')).toBeInTheDocument())
})
