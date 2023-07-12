import React from 'react'
import { render, screen } from '@testing-library/react'
import HtmlPageView from './HtmlPageView'
import { HtmlPage, HtmlSection } from 'api/api'
import { usePortalEnv } from 'providers/PortalProvider'

jest.mock('providers/PortalProvider', () => {
  return {
    ...jest.requireActual('providers/PortalProvider'),
    usePortalEnv: jest.fn()
  }
})


describe('HTMLPageView', () => {
  beforeEach(() => {
    // @ts-expect-error "TS doesn't know about mocks"
    usePortalEnv.mockReturnValue({
      portal: { name: 'Test portal' }
    })
  })

  it('handles trivial landing page', () => {
    const simplePage: HtmlPage = {
      sections: [],
      title: 'Page title',
      path: ''
    }
    const { container } = render(<HtmlPageView page={simplePage}/>)
    expect(container).toBeEmptyDOMElement()
  })

  it('handles landing page with one section', () => {
    const simplePage: HtmlPage = {
      sections: [{
        sectionType: 'RAW_HTML',
        anchorRef: 'foo',
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
})
