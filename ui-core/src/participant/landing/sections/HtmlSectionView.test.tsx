import React from 'react'
import { render } from '@testing-library/react'
import { HtmlSectionView } from './HtmlSectionView'
import { HtmlSection, SectionType } from 'src/types/landingPageConfig'


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

    it('renders titles as markdown', () => {
      const section: HtmlSection = {
        id: 'sectionWithMarkdown',
        sectionType: 'HERO_WITH_IMAGE',
        sectionConfig: '{"title": "**Title**"}'
      }

      const { container } = render(<HtmlSectionView section={section} />)
      expect(container).toHaveTextContent('Title')
      expect(container).not.toHaveTextContent('**Title**')
    })

    it.each([
      'FAQ',
      'HERO_CENTERED',
      'HERO_WITH_IMAGE',
      'PARTICIPATION_DETAIL',
      'PHOTO_BLURB_GRID',
      'STEP_OVERVIEW'
    ])('renders titles as markdown for %s', sectionType => {
      const section: HtmlSection = {
        id: `sectionWithMarkdown-${sectionType}`,
        sectionType: sectionType as SectionType,
        sectionConfig: '{"title": "**Title**", "questions": []}'
      }

      const { container } = render(<HtmlSectionView section={section} />)
      expect(container).toHaveTextContent('Title')
      expect(container).not.toHaveTextContent('**Title**')
    })

    it.each([
      'FAQ',
      'HERO_CENTERED',
      'HERO_WITH_IMAGE',
      'PARTICIPATION_DETAIL'
    ])('renders blurbs as markdown for %s', sectionType => {
      const section: HtmlSection = {
        id: `sectionWithMarkdown-${sectionType}`,
        sectionType: sectionType as SectionType,
        sectionConfig: '{"blurb": "**Blurb**", "questions": []}'
      }

      const { container } = render(<HtmlSectionView section={section} />)
      expect(container).toHaveTextContent('Blurb')
      expect(container).not.toHaveTextContent('**Blurb**')
    })
  })
})
