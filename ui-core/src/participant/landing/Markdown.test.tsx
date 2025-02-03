import { render, screen } from '@testing-library/react'
import React from 'react'

import { InlineMarkdown, Markdown } from './Markdown'

describe('Markdown', () => {
  it('renders links to open in new tab', () => {
    render(<Markdown>Some [link](https://example.com) is here</Markdown>)
    expect(screen.getByText('link')).toHaveAttribute('target', '_blank')
  })

  it('renders relative links not to open in new tab', () => {
    render(<Markdown>Some [link](/other/page) is here</Markdown>)
    expect(screen.getByText('link')).toHaveAttribute('target', '')
  })

  it('renders inline links to open in new tab', () => {
    render(<InlineMarkdown>Some [link](https://example.com) is here</InlineMarkdown>)
    expect(screen.getByText('link')).toHaveAttribute('target', '_blank')
  })
})
