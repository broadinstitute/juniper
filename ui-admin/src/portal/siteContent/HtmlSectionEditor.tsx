import React, { useState } from 'react'
import {HtmlPage, HtmlSection, SectionType} from '@juniper/ui-core'
import Select from 'react-select'
import { isEmpty } from 'lodash'
import { IconButton } from 'components/forms/Button'
import { faChevronDown, faChevronUp, faTimes } from '@fortawesome/free-solid-svg-icons'

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
  htmlPage,
  updatePage,
  section,
  sectionIndex,
  readOnly
}: {
  htmlPage: HtmlPage,
  updatePage: (page: HtmlPage) => void,
  section: HtmlSection
  sectionIndex: number
  readOnly: boolean
}) => {
  const sectionConfig = JSON.stringify(JSON.parse(section?.sectionConfig ?? '{}'), null, 2)
  const initial = SECTION_TYPES.find(sectionType => sectionType.value === section.sectionType)
  const [sectionTypeOpt, setSectionTypeOpt] = useState(initial)

  const updateSection = (sectionIndex: number, updatedSection: HtmlSection) => {
    try {
      JSON.parse(updatedSection.sectionConfig ?? '{}')
    } catch (e) {
      // for now, we just don't allow changing the object structure itself -- just plain text edits
      return
    }

    const newSection = {
      ...htmlPage.sections[sectionIndex],
      sectionType: updatedSection.sectionType,
      sectionConfig: updatedSection.sectionConfig
    }
    const newSectionArray = [...htmlPage.sections]
    newSectionArray[sectionIndex] = newSection
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    updatePage(htmlPage)
  }

  const removeSection = (sectionIndex: number) => {
    const newSectionArray = [...htmlPage.sections]
    newSectionArray.splice(sectionIndex, 1)
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    updatePage(htmlPage)
  }

  const moveSection = (sectionIndex: number, direction: 'up' | 'down') => {
    if (sectionIndex === 0 && direction === 'up') { return }
    const newSectionArray = [...htmlPage.sections]
    const sectionToMove = newSectionArray[sectionIndex]
    newSectionArray.splice(sectionIndex, 1)
    if (direction === 'up') {
      newSectionArray.splice(sectionIndex - 1, 0, sectionToMove)
    } else {
      newSectionArray.splice(sectionIndex + 1, 0, sectionToMove)
    }
    htmlPage = {
      ...htmlPage,
      sections: newSectionArray
    }
    updatePage(htmlPage)
  }

  return <>
    <div className="d-flex flex-grow-1 mb-1">
      {/* Right now we do not support changing the type for an existing section. The way to identify if a
        section has been previously saved is to look at the id. If it's empty, it's a new section, and we can
        allow the user to change the type. */ }
      <Select className='w-100' options={SECTION_TYPES} value={sectionTypeOpt} aria-label={'Select section type'}
        isDisabled={readOnly || !isEmpty(section.id)}
        onChange={opt => {
          if (opt != undefined) {
            setSectionTypeOpt(opt)
            updateSection(sectionIndex, { ...section, sectionType: opt.value as SectionType })
          }
        }}/>
      <IconButton
        aria-label="Move this section before the previous one"
        className="ms-2"
        disabled={readOnly || sectionIndex === 0}
        icon={faChevronUp}
        variant="light"
        onClick={() => {
          moveSection(sectionIndex, 'up')
        }}
      />
      <IconButton
        aria-label="Move this section after the next one"
        className="ms-2"
        disabled={readOnly}
        icon={faChevronDown}
        variant="light"
        onClick={() => {
          moveSection(sectionIndex, 'down')
        }}
      />
      <IconButton
        aria-label="Delete this section"
        className="ms-2"
        disabled={readOnly}
        icon={faTimes}
        variant="light"
        onClick={() => removeSection(sectionIndex)}
      />
    </div>
    <textarea value={sectionConfig} style={{ height: 'calc(100% - 2em)', width: '100%' }}
      readOnly={readOnly}
      onChange={e => updateSection(sectionIndex, { ...section, sectionConfig: e.target.value })}/>
  </>
}

export default HtmlSectionEditor
