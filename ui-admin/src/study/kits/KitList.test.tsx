import React from 'react'
import { act, render, screen } from '@testing-library/react'
import { mockEnrollee, mockKitRequest, mockStudyEnvContext } from 'test-utils/mocking-utils'
import KitList from './KitList'
import { BrowserRouter } from 'react-router-dom'
import userEvent from '@testing-library/user-event'

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
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()

    await act(async () => render(
      <BrowserRouter>
        <KitList studyEnvContext={studyEnvContext}/>
      </BrowserRouter>
    ))

    expect(screen.getByText('JOSALK')).toBeInTheDocument()
    expect(screen.getByText('Test kit')).toBeInTheDocument()
  })
})
