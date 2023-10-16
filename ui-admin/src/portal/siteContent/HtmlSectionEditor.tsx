import React, { useState } from 'react'
import { HtmlSection, SectionType } from '@juniper/ui-core'
import Select from 'react-select'
import { isEmpty } from 'lodash'

const SECTION_TYPES = [
  { label: 'FAQ', value: 'FAQ' },
  { label: 'HERO_CENTERED', value: 'HERO_CENTERED' },
  { label: 'HERO_WITH_IMAGE', value: 'HERO_WITH_IMAGE' },
  { label: 'SOCIAL_MEDIA', value: 'SOCIAL_MEDIA' },
  { label: 'STEP_OVERVIEW', value: 'STEP_OVERVIEW' },
  { label: 'PHOTO_BLURB_GRID', value: 'PHOTO_BLURB_GRID' },
  { label: 'PARTICIPATION_DETAIL', value: 'PARTICIPATION_DETAIL' },
  { label: 'RAW_HTML', value: 'RAW_HTML' },
  { label: 'LINK_SECTIONS_FOOTER', value: 'LINK_SECTIONS_FOOTER' },
  { label: 'BANNER_IMAGE', value: 'BANNER_IMAGE' }
]

/**
 * Returns an editor for an HtmlSection
 */
const HtmlSectionEditor = ({
  section,
  sectionIndex,
  readOnly,
  updateSection
}: {
  section: HtmlSection
  sectionIndex: number
  readOnly: boolean
  updateSection: (sectionIndex: number, updatedSection: HtmlSection) => void
}) => {
  const sectionConfig = JSON.stringify(JSON.parse(section?.sectionConfig ?? '{}'), null, 2)
  const initial = SECTION_TYPES.find(sectionType => sectionType.value === section.sectionType)
  const [sectionTypeOpt, setSectionTypeOpt] = useState(initial)

  return <>
    <div>
      {/* Right now we do not support changing the type for an existing section. The way to identify if a
          section has been previously saved is to look at the id. If it's empty, it's a new section, and we can
          allow the user to change the type. */ }
      <Select options={SECTION_TYPES} value={sectionTypeOpt} aria-label={'Select section type'}
        isDisabled={readOnly || !isEmpty(section.id)}
        onChange={opt => {
          if (opt != undefined) {
            setSectionTypeOpt(opt)
            updateSection(sectionIndex, { ...section, sectionType: opt.value as SectionType })
          }
        }}/>
    </div>
    <textarea value={sectionConfig} style={{ height: 'calc(100% - 2em)', width: '100%' }}
      readOnly={readOnly}
      onChange={e => updateSection(sectionIndex, { ...section, sectionConfig: e.target.value })}/>
  </>
}

export default HtmlSectionEditor
