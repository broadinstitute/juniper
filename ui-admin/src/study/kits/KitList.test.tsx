import React from 'react'
import { act, render, screen, waitFor } from '@testing-library/react'
import { mockEnrollee, mockKitRequest, mockStudyEnvContext } from 'test-utils/mocking-utils'
import KitList from './KitList'
import { BrowserRouter } from 'react-router-dom'

import { userEvent } from '@testing-library/user-event'
import Api from 'api/api'
import { ReactNotifications } from 'react-notifications-component'
import { MockSuperuserProvider } from 'test-utils/user-mocking-utils'

describe('KitList', () => {
  it('gracefully handles unexpected JSON from Pepper', async () => {
    mockFetchKits()
    const studyEnvContext = mockStudyEnvContext()

    await act(async () => render(
      <BrowserRouter>
        <KitList studyEnvContext={studyEnvContext}/>
      </BrowserRouter>
    ))

    expect(screen.getByText('JOSALK')).toBeInTheDocument()
    expect(screen.getByText('Test kit')).toBeInTheDocument()
  })

  it('indicates refresh failed', async () => {
    mockFetchKits()
    jest.spyOn(Api, 'refreshKitStatuses').mockImplementation(() => {
      throw 'failed arrgh'
    })
    const studyEnvContext = mockStudyEnvContext()
    await act(async () => render(
      <BrowserRouter>
        <MockSuperuserProvider>
          <ReactNotifications />
          <KitList studyEnvContext={studyEnvContext}/>
        </MockSuperuserProvider>
      </BrowserRouter>
    ))
    userEvent.click(screen.getByText('Refresh'))
    await waitFor(() => expect(screen.getByText('kit statuses could not be refreshed'))
      .toBeInTheDocument())
  })
})

function mockFetchKits() {
  jest.spyOn(Api, 'fetchKitsByStudyEnvironment').mockResolvedValue(
    [mockKitRequest({
      enrolleeShortcode: mockEnrollee().shortcode,
      status: 'CREATED'
    })]
  )
}
