import React, { useEffect, useState } from 'react'
import { HtmlPage, HtmlSection, SectionType } from '@juniper/ui-core'
import Select from 'react-select'
import { isEmpty } from 'lodash'
import { IconButton } from 'components/forms/Button'
import { faChevronDown, faChevronUp, faTimes } from '@fortawesome/free-solid-svg-icons'
import { sectionTemplates } from './sectionTemplates'
import classNames from 'classnames'

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
  onUpdate,
  removeSection,
  moveSection,
  section,
  sectionIndex,
  siteInvalid,
  setSiteInvalid,
  readOnly
}: {
  onUpdate: (section: HtmlSection) => void
  removeSection?: (sectionIndex: number) => void
  moveSection?: (direction: 'up' | 'down') => void
  section: HtmlSection
  sectionIndex: number
  siteInvalid: boolean
  setSiteInvalid: (invalid: boolean) => void
  readOnly: boolean
}) => {
  const [sectionContainsErrors, setSectionContainsErrors] = useState(false)
  const initial = SECTION_TYPES.find(sectionType => sectionType.value === section.sectionType)
  const [sectionTypeOpt, setSectionTypeOpt] = useState(initial)

  const [editorValue, setEditorValue] = useState(() =>
    JSON.stringify(JSON.parse(section?.sectionConfig ?? '{}'), null, 2))

  useEffect(() => {
    setEditorValue(JSON.stringify(JSON.parse(section?.sectionConfig ?? '{}'), null, 2))
  }, [section.sectionConfig])

  console.log(section)

  const handleEditorChange = (newEditorValue: string) => {
    setEditorValue(newEditorValue)

    try {
      JSON.parse(newEditorValue)
      setSiteInvalid(false)
      setSectionContainsErrors(false)
      onUpdate({ ...section, sectionConfig: newEditorValue })
    } catch (e) {
      setSiteInvalid(true)
      setSectionContainsErrors(true)
      // Note that we do not call updateSection here, as that would result in an invalid preview being shown.
      // Instead, the preview will be based on the last valid config for this section.
    }
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
            if (sectionContainsErrors) {
              //If the user is changing the section that had errors, then we can clear the siteInvalid flag
              //because it will now be using a valid default template.
              setSiteInvalid(false)
              setSectionContainsErrors(false)
            }
            const sectionTemplate = JSON.stringify(sectionTemplates[opt.label])
            setSectionTypeOpt(opt)
            onUpdate({
              ...section,
              sectionType: opt.value as SectionType,
              sectionConfig: sectionTemplate
            })
          }
        }}/>
      { moveSection && <IconButton
        aria-label="Move this section before the previous one"
        className="ms-2"
        disabled={readOnly || sectionIndex === 0}
        icon={faChevronUp}
        variant="light"
        onClick={() => {
          moveSection('up')
        }}
      /> }
      { moveSection && <IconButton
        aria-label="Move this section after the next one"
        className="ms-2"
        disabled={readOnly || siteInvalid}
        icon={faChevronDown}
        variant="light"
        onClick={() => {
          moveSection('down')
        }}
      /> }
      { removeSection && <IconButton
        aria-label="Delete this section"
        className="ms-2"
        disabled={readOnly}
        icon={faTimes}
        variant="light"
        onClick={() => removeSection(sectionIndex)}
      /> }
    </div>
    <textarea value={editorValue} style={{ height: 'calc(100% - 2em)', width: '100%', minHeight: '300px' }}
      disabled={readOnly || (siteInvalid && !sectionContainsErrors)}
      className={classNames('w-100 flex-grow-1 form-control font-monospace',
        { 'is-invalid': sectionContainsErrors })}
      onChange={e => {
        handleEditorChange(e.target.value)
      }}/>
  </>
}

export default HtmlSectionEditor
