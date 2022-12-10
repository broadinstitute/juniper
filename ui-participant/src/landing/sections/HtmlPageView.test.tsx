import React from 'react'
import { render, screen } from '@testing-library/react'
import HtmlPageView from './HtmlPageView'
import { HtmlPage } from 'api/api'


test('handles trivial landing page', () => {
  const simplePage: HtmlPage = {
    sections: [],
    title: 'Page title',
    path: ''
  }
  const { container } = render(<HtmlPageView page={simplePage}/>)
  expect(container).toBeEmptyDOMElement()
})

test('handles landing page with one section', () => {
  const simplePage: HtmlPage = {
    sections: [{
      sectionType: 'RAW_HTML',
      rawContent: '<div>Hellllo</div>',
      id: 'fakeId',
      sectionConfig: null
    }],
    title: 'Page title',
    path: ''
  }
  render(<HtmlPageView page={simplePage}/>)
  expect(screen.getByText('Hellllo')).toBeInTheDocument()
})
