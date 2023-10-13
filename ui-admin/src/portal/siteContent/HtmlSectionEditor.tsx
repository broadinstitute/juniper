import React, { useState } from 'react'
import { HtmlSection } from '@juniper/ui-core'
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
  updateSectionConfig
}: {
  section: HtmlSection
  sectionIndex: number
  readOnly: boolean
  updateSectionConfig: (sectionIndex: number, newConfig: string) => void
}) => {
  const textValue = JSON.stringify(JSON.parse(section?.sectionConfig ?? '{}'), null, 2)
  const initial = SECTION_TYPES.find(sectionType => sectionType.value === section.sectionType)
  const [sectionTypeOpt, setSectionTypeOpt] = useState(initial)

  return <>
    <div>
      <Select options={SECTION_TYPES} value={sectionTypeOpt}
        onChange={e => {
          if (!isEmpty(section.id)) {
            setSectionTypeOpt(sectionTypeOpt)
          } else {
            setSectionTypeOpt(e!)
            updateSectionConfig(
              sectionIndex,
              JSON.stringify({ ...JSON.parse(textValue), sectionType: e?.value }, null, 2)
            )
          }
        }}/>
    </div>
    <textarea value={textValue} style={{ height: 'calc(100% - 2em)', width: '100%' }}
      readOnly={readOnly}
      onChange={e => updateSectionConfig(sectionIndex, e.target.value)}/>
  </>
}

export default HtmlSectionEditor
