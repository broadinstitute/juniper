import React from 'react'
import { act, render, screen } from '@testing-library/react'
import { mockEnrollee, mockKitRequest, mockStudyEnvContext } from 'test-utils/mocking-utils'
import KitList from './KitList'
import { BrowserRouter } from 'react-router-dom'

const dsmStatus = JSON.stringify({
  kitId: '123-456-789',
  currentStatus: 'CREATED',
  labelDate: new Date().toISOString(),
  scanDate: new Date().toISOString(),
  receiveDate: new Date().toISOString(),
  trackingNumber: 'Z23456',
  returnTrackingNumber: 'Z87654'
})

jest.mock('api/api', () => ({
  fetchKitsByStudyEnvironment: () => {
    return Promise.resolve([mockKitRequest({
      enrollee: mockEnrollee(),
      dsmStatus: '{"unexpected": "boom"}'
    })])
  }
}))

describe('KitList', () => {
  it('gracefully handles unexpected JSON from Pepper', async () => {
    const studyEnvContext = mockStudyEnvContext()

    await act(async () => render(
      <BrowserRouter>
        <KitList studyEnvContext={studyEnvContext}/>
      </BrowserRouter>
    ))

    // screen.debug()

    expect(screen.getByText('(unknown)')).toBeInTheDocument()
  })
})
