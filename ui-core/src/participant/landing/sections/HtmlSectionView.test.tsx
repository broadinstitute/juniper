import React from 'react'
import { render, screen } from '@testing-library/react'
import { HtmlSectionView } from './HtmlSectionView'
import { HtmlSection } from 'src/types/landingPageConfig'


jest.mock('providers/PortalProvider', () => {
  return {
    ...jest.requireActual('providers/PortalProvider'),
    usePortalEnv: jest.fn()
  }
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
