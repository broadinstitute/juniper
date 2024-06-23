import { HtmlSection, SectionConfig } from '@juniper/ui-core'
import { Textarea } from 'components/forms/Textarea'
import React from 'react'

/**
 * Returns an editor for the blurb element of a website section
 */
export const BlurbEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  return (
    <Textarea rows={3} className="mb-2" label="Blurb" value={config.blurb as string}
      placeholder={'Enter a blurb for this section'}
      onChange={value => {
        const parsed = JSON.parse(section.sectionConfig || '{}')
        updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, blurb: value }) })
      }}/>
  )
}
