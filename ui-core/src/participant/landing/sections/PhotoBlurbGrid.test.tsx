import React from 'react'
import { render, screen } from '@testing-library/react'
import { PhotoBioView } from 'src/participant/landing/sections/PhotoBlurbGrid'
import { userEvent } from '@testing-library/user-event'


describe('PhotoBlurbGrid', () => {
  it('details are viewable on click', async () => {
    const bio = {
      'name': 'Anthony Philippakis',
      'title': 'MD, PhD',
      'blurb': 'Institute Scientist, Broad Institute',
      'detail': 'General Partner, GV',
      'image': { 'cleanFileName': 'anthony_philippakis.jpg', 'version': 1 }
    }

    render(<PhotoBioView photoBio={bio}/>)
    expect(screen.getByAltText('Anthony Philippakis')).toBeInTheDocument()
    expect(screen.queryByText('General Partner, GV')).not.toBeInTheDocument()
    await userEvent.click(screen.getByAltText('Anthony Philippakis'))
    expect(screen.queryByText('General Partner, GV')).toBeInTheDocument()
  })
})
