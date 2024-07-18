import React from 'react'
import {  mockStudyEnvContext, renderInPortalRouter } from 'test-utils/mocking-utils'
import WithdrawnEnrolleeList from './WithdrawnEnrolleeList'
import Api from 'api/api'
import { waitFor, screen } from '@testing-library/react'


test('renders list', async () => {
  const studyEnvContext = mockStudyEnvContext()
  jest.spyOn(Api, 'fetchWithdrawnEnrollees').mockResolvedValue([
    { shortcode: 'BLEH', userData: '{"username": "foo@bar.com", "createdAt": 0}', createdAt: 123 }
  ])
  renderInPortalRouter(studyEnvContext.portal,
    <WithdrawnEnrolleeList studyEnvContext={studyEnvContext}  />)
  await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
  expect(screen.getByText('BLEH')).toBeInTheDocument()
  // email should be hidden by default
  expect(screen.queryByText('foo@bar.com')).not.toBeInTheDocument()
})
