import React from 'react'
import { render, screen } from '@testing-library/react'
import HtmlPageView, { HtmlSectionView } from './HtmlPageView'
import { HtmlPage, HtmlSection } from 'api/api'


describe('HTMLPageView', () => {
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

describe('HTMLSectionView', () => {
  describe('misconfiguration does not cause render errors', () => {
    beforeEach(() => {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      jest.spyOn(console, 'warn').mockImplementation(() => {})
    })

    it('absorbs configuration that is invalid JSON', () => {
      const section: HtmlSection = {
        id: 'misconfiguredSection',
        sectionType: 'HERO_WITH_IMAGE',
        sectionConfig: '{key:"value"}'
      }

      const { container } = render(<HtmlSectionView section={section} />)
      expect(container).toBeEmptyDOMElement()
      expect(console.warn).toHaveBeenCalled()
    })

    it('absorbs configuration that is not an object', () => {
      const section: HtmlSection = {
        id: 'misconfiguredSection',
        sectionType: 'HERO_WITH_IMAGE',
        sectionConfig: '"config"'
      }

      const { container } = render(<HtmlSectionView section={section} />)
      expect(container).toBeEmptyDOMElement()
      expect(console.warn).toHaveBeenCalled()
    })
  })
})
