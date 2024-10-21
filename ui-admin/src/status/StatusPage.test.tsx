import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { StatusPage } from './StatusPage'
import Api from 'api/api'
import { asMockedFn } from '@juniper/ui-core'

jest.mock('api/api')

describe('StatusPage', () => {
  it('renders operational status when system is up', async () => {
    asMockedFn(Api.getSystemStatus).mockResolvedValue({ ok: true, systems: [] })
    render(<StatusPage />)

    await waitFor(() => {
      expect(screen.getByText('operational')).toBeInTheDocument()
    })
  })

  it('renders degraded status when system is down', async () => {
    asMockedFn(Api.getSystemStatus).mockResolvedValue({ ok: false, systems: [] })
    render(<StatusPage />)

    await waitFor(() => {
      expect(screen.getByText('degraded')).toBeInTheDocument()
    })
  })
})
