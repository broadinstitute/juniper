import { render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import InfoPopup from './InfoPopup'

describe('InfoPopup', () => {
  it('renders a clickable tooltip button', async () => {
    render(<div>
      <InfoPopup content={'blah blah'}/>
    </div>)
    // tooltip doesn't appear by default
    expect(screen.queryByText('blah blah')).toBeNull()

    const button = screen.getByRole('button')
    await userEvent.click(button)
    // tooltip appears on click
    expect(await screen.findByText('blah blah')).toBeTruthy()
    await userEvent.click(button)
    // tooltip disappears on second click
    expect(screen.queryByText('blah blah')).toBeNull()
  })

  it('disappears on clicks outside', async () => {
    render(<div>
      <span>Something complicated</span>
      <InfoPopup content={'blah blah'}/>
    </div>)
    const button = screen.getByRole('button')
    await userEvent.click(button)
    // tooltip appears on click
    expect(await screen.findByText('blah blah')).toBeTruthy()

    // tooltip disappears on click of outside thing
    await userEvent.click(screen.getByText('Something complicated'))
    expect(screen.queryByText('blah blah')).toBeNull()
  })
})
