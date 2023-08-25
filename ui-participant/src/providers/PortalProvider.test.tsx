import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import Api from 'api/api'
import PortalProvider from './PortalProvider'

describe('PortalProvider', () => {
  it('shows support email on non-existent portal', async () => {
    jest.spyOn(Api, 'getPortal').mockImplementation(() => Promise.reject(null))
    render(<PortalProvider><span>some stuff</span></PortalProvider>)

    await waitFor(() => expect(screen.getByText('support@juniper.terra.bio')).toBeInTheDocument())
    expect(screen.queryByText('some stuff')).not.toBeInTheDocument()
  })
})
