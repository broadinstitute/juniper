import React from 'react'
import Api from 'api/api'
import { render, screen, waitFor } from '@testing-library/react'
import SiteContentVersionSelector from './SiteContentVersionSelector'
import { select } from 'react-select-event'
import { userEvent } from '@testing-library/user-event'
import { mockPortalEnvironment } from '../../test-utils/mocking-utils'

const mockSiteContents = [
  {
    id: 'fakeId1',
    stableId: 'foo',
    version: 1,
    localizedSiteContents: [],
    defaultLanguage: 'en',
    createdAt: 0
  },
  {
    id: 'fakeId2',
    stableId: 'foo',
    version: 2,
    localizedSiteContents: [],
    defaultLanguage: 'en',
    createdAt: 4
  }
]

test('can select a site content version', async () => {
  jest.spyOn(Api, 'getSiteContentVersions').mockResolvedValue(mockSiteContents)
  const loadSiteContentFn = jest.fn()

  render(<SiteContentVersionSelector stableId="foo" loadSiteContent={loadSiteContentFn} portalShortcode="portal"
    current={mockSiteContents[0]} onDismiss={jest.fn()}  switchToVersion={jest.fn()}
    portalEnv={mockPortalEnvironment('sandbox')}/>)
  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
  expect(screen.getByText('You are currently editing version 1,')).toBeInTheDocument()
  await select(screen.getByLabelText('Select version'), ['2'])

  await userEvent.click(screen.getByText('Edit version 2'))
  expect(loadSiteContentFn).toHaveBeenCalledTimes(1)
  expect(loadSiteContentFn).toHaveBeenCalledWith('foo', 2)
})

test('does not show switch option for non-sandbox environments', async () => {
  jest.spyOn(Api, 'getSiteContentVersions').mockResolvedValue(mockSiteContents)
  const loadSiteContentFn = jest.fn()

  render(<SiteContentVersionSelector stableId="foo" loadSiteContent={loadSiteContentFn} portalShortcode="portal"
    current={mockSiteContents[0]} onDismiss={jest.fn()} switchToVersion={jest.fn()}
    portalEnv={mockPortalEnvironment('irb')}/>)
  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
  await select(screen.getByLabelText('Select version'), ['2'])
  expect(screen.queryByText('Switch sandbox to version 2')).not.toBeInTheDocument()
})
test('shows switch option for sandbox environments', async () => {
  jest.spyOn(Api, 'getSiteContentVersions').mockResolvedValue(mockSiteContents)
  const loadSiteContentFn = jest.fn()

  render(<SiteContentVersionSelector stableId="foo" loadSiteContent={loadSiteContentFn} portalShortcode="portal"
    current={mockSiteContents[0]} onDismiss={jest.fn()}  switchToVersion={jest.fn()}
    portalEnv={mockPortalEnvironment('sandbox')}/>)
  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
  expect(screen.queryByText('Switch to version 2')).not.toBeInTheDocument()
  await select(screen.getByLabelText('Select version'), ['2'])

  expect(screen.getByText('Switch sandbox to version 2')).toBeInTheDocument()
})
