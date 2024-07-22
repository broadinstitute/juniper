import React from 'react'

import { render, screen, waitFor } from '@testing-library/react'
import { mockLocalSiteContent } from 'test-utils/mock-site-content'
import Api from 'api/api'
import BrandingModal from './BrandingModal'
import { userEvent } from '@testing-library/user-event'
import { select } from 'react-select-event'

describe('BrandingModal', () => {
  test('branding color is updatable', async () => {
    jest.spyOn(Api, 'getPortalMedia').mockResolvedValue([])
    const updateSpy = jest.fn()
    render(<BrandingModal portalShortcode="test"
      onDismiss={jest.fn()}
      updateLocalContent={updateSpy}
      localContent={{
        ...mockLocalSiteContent(),
        primaryBrandColor: '#ff0000',
        dashboardBackgroundColor: '#00ff00'
      }}/>)
    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    expect(screen.getByTitle('color preview')).toHaveStyle('background-color: #ff0000')
    expect(screen.getByTitle('dashboard background preview')).toHaveStyle('background-color: #00ff00')
    await userEvent.clear(screen.getByLabelText('Primary Brand Color'))
    await userEvent.type(screen.getByLabelText('Primary Brand Color'), '#00ff00')
    await userEvent.clear(screen.getByLabelText('Dashboard Background Color'))
    await userEvent.type(screen.getByLabelText('Dashboard Background Color'), '#0000ff')
    await userEvent.click(screen.getByText('Ok'))
    expect(updateSpy).toHaveBeenCalledWith(expect.objectContaining({
      primaryBrandColor: '#00ff00',
      dashboardBackgroundColor: '#0000ff'
    }))
  })

  test('nav logo is updatable', async () => {
    jest.spyOn(Api, 'getPortalMedia').mockResolvedValue([{
      cleanFileName: 'test2.png', version: 1, id: 'teest', createdAt: 0
    }])
    const updateSpy = jest.fn()
    render(<BrandingModal portalShortcode="test"
      onDismiss={jest.fn()}
      updateLocalContent={updateSpy}
      localContent={{
        ...mockLocalSiteContent(),
        primaryBrandColor: '#ff0000'
      }}/>)
    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    await select(screen.getByLabelText('Navbar logo'), 'test2.png')
    await userEvent.click(screen.getByText('Ok'))
    expect(updateSpy).toHaveBeenCalledWith(expect.objectContaining({ navLogoCleanFileName: 'test2.png' }))
  })
})
