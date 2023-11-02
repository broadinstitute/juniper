import { render, screen } from '@testing-library/react'
import React from 'react'

import ConfiguredMedia from './ConfiguredMedia'

describe('ConfiguredMedia', () => {
  it('renders iframe for valid video link', () => {
    render(<ConfiguredMedia media={{ videoLink: 'https://youtube.com/someVideo' }}/>)
    expect(screen.getByTestId('media-iframe')).toBeInTheDocument()
  })

  it('does not render iframe for unapproved video link', () => {
    render(<ConfiguredMedia media={{ videoLink: 'http://crime.org' }}/>)
    expect(screen.queryByTestId('media-iframe')).not.toBeInTheDocument()
    expect(screen.getByText('Disallowed video source')).toBeInTheDocument()
  })

  it('does not render iframe for malformatted urls', () => {
    render(<ConfiguredMedia media={{ videoLink: 'some.site' }}/>)
    expect(screen.queryByTestId('media-iframe')).not.toBeInTheDocument()
    expect(screen.getByText('Disallowed video source')).toBeInTheDocument()
  })

  it('renders image if image specified', () => {
    render(<ConfiguredMedia media={{ cleanFileName: 'foo.png', version: 1, alt: 'testImage' }}/>)
    expect(screen.getByAltText('testImage')).toBeInTheDocument()
    expect(screen.queryByTestId('media-iframe')).not.toBeInTheDocument()
  })
})
