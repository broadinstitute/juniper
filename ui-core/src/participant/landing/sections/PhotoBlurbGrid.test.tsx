import React from 'react'
import { render, screen } from '@testing-library/react'
import { PhotoBioView } from 'src/participant/landing/sections/PhotoBlurbGrid'
import userEvent from '@testing-library/user-event'


describe('PhotoBlurbGrid', () => {
  it('details are viewable on click', async () => {
    const bio = {
      'name': 'Anthony Philippakis',
      'title': 'MD, PhD',
      'blurb': 'Institute Scientist, Broad Institute',
      'detail': 'General Partner, GV',
      'image': { 'cleanFileName': 'anthony_philippakis.jpg', 'version': 1 }
    }

    const { container } = render(<PhotoBioView photoBio={bio}/>)
    expect(container).toHaveTextContent('Anthony Philippakis')
    expect(container).not.toHaveTextContent('General Partner, GV')
    await userEvent.click(screen.getByText('Anthony Philippakis'))
    expect(container).toHaveTextContent('General Partner, GV')
  })
})
