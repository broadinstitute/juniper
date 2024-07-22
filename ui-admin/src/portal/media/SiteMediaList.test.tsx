import React from 'react'

import { render, screen, waitFor } from '@testing-library/react'
import { mockPortalContext, mockPortalEnvironment } from 'test-utils/mocking-utils'
import SiteMediaList from './SiteMediaList'
import Api from 'api/api'
import { userEvent } from '@testing-library/user-event'
import { setupRouterTest } from '@juniper/ui-core'

test('renders empty table', async () => {
  jest.spyOn(Api, 'getPortalMedia').mockResolvedValue([])
  const { RoutedComponent } = setupRouterTest(
    <SiteMediaList portalContext={mockPortalContext()}
      portalEnv={mockPortalEnvironment('sandbox')}/>)
  render(RoutedComponent)
  await waitFor(() => expect(screen.getByText('Showing 0 of 0 rows')).toBeInTheDocument())
})


test('renders table with a clickable image', async () => {
  jest.spyOn(Api, 'getPortalMedia').mockResolvedValue([{
    id: 'fakeId',
    cleanFileName: 'testImage.png',
    version: 1,
    createdAt: Date.now()
  }])
  const { RoutedComponent } = setupRouterTest(
    <SiteMediaList portalContext={mockPortalContext()}
      portalEnv={mockPortalEnvironment('sandbox')}/>)
  render(RoutedComponent)
  await waitFor(() => expect(screen.getByText('Showing 1 of 1 rows')).toBeInTheDocument())
  expect(screen.getByText('testImage.png')).toBeInTheDocument()
  expect(screen.queryByAltText('full-size preview of testImage.png')).not.toBeInTheDocument()
  // clicking the image should popup the full-size preview
  await userEvent.click(screen.getByTitle('show full-size preview'))
  await waitFor(() => expect(screen.getByAltText('full-size preview of testImage.png')).toBeInTheDocument())
})

test('does not render a preview for a non-image type', async () => {
  jest.spyOn(Api, 'getPortalMedia').mockResolvedValue([{
    id: 'fakeId',
    cleanFileName: 'testDoc.pdf',
    version: 1,
    createdAt: Date.now()
  }])
  const { RoutedComponent } = setupRouterTest(
    <SiteMediaList portalContext={mockPortalContext()}
      portalEnv={mockPortalEnvironment('sandbox')}/>)
  render(RoutedComponent)
  await waitFor(() => expect(screen.getByText('Showing 1 of 1 rows')).toBeInTheDocument())
  expect(screen.getByText('testDoc.pdf')).toBeInTheDocument()
  expect(screen.getByText('no preview')).toBeInTheDocument()
})
