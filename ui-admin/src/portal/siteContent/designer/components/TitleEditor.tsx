import { HtmlSection, SectionConfig } from '@juniper/ui-core'
import { TextInput } from 'components/forms/TextInput'
import React from 'react'

/**
 * Returns an editor for the title element of a website section
 */
export const TitleEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  return (
    <TextInput className="mb-2" label="Title" value={config.title as string}
      placeholder={'Enter a title for this section'}
      onChange={value => {
        const parsed = JSON.parse(section.sectionConfig || '{}')
        updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, title: value }) })
      }}/>
  )
}
