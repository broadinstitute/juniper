import React from 'react'
import Api from 'api/api'
import { render, screen, waitFor } from '@testing-library/react'
import SiteContentVersionSelector from './SiteContentVersionSelector'
import { select } from 'react-select-event'
import userEvent from '@testing-library/user-event'

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
  jest.spyOn(Api, 'getSiteContentVersions').mockImplementation(() => Promise.resolve(mockSiteContents))
  const loadSiteContentFn = jest.fn()

  render(<SiteContentVersionSelector stableId="foo" loadSiteContent={loadSiteContentFn} portalShortcode="portal"
    current={mockSiteContents[0]} onDismiss={jest.fn()}/>)
  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
  expect(screen.getByText('You are currently editing version 1,')).toBeInTheDocument()
  await select(screen.getByLabelText('Select version to edit'), ['2'])

  await userEvent.click(screen.getByText('Edit version 2'))
  expect(loadSiteContentFn).toHaveBeenCalledTimes(1)
  expect(loadSiteContentFn).toHaveBeenCalledWith('foo', 2)
})
