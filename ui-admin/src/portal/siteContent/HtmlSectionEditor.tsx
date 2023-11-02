import React, { useEffect, useState } from 'react'
import { HtmlSection, SectionType } from '@juniper/ui-core'
import Select from 'react-select'
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
  updateSection,
  removeSection,
  moveSection,
  section,
  siteHasInvalidSection,
  setSiteHasInvalidSection,
  allowTypeChange,
  readOnly
}: {
  updateSection: (section: HtmlSection) => void
  removeSection?: () => void
  moveSection?: (direction: 'up' | 'down') => void
  section: HtmlSection
  siteHasInvalidSection: boolean
  setSiteHasInvalidSection: (invalid: boolean) => void
  allowTypeChange: boolean
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

  const handleEditorChange = (newEditorValue: string) => {
    setEditorValue(newEditorValue)

    try {
      JSON.parse(newEditorValue)
      setSiteHasInvalidSection(false)
      setSectionContainsErrors(false)
      updateSection({ ...section, sectionConfig: newEditorValue })
    } catch (e) {
      setSiteHasInvalidSection(true)
      setSectionContainsErrors(true)
      // Note that we do not call updateSection here, as that would result in an invalid preview being shown.
      // Instead, the preview will be based on the last valid config for this section.
    }
  }

  return <>
    <div className="d-flex flex-grow-1 mb-1">
      <Select className='w-100' options={SECTION_TYPES} value={sectionTypeOpt} aria-label={'Select section type'}
        isDisabled={readOnly || !allowTypeChange}
        onChange={opt => {
          if (opt != undefined) {
            if (sectionContainsErrors) {
              //If the user is changing the section that had errors, then we can clear the siteHasInvalidSection flag
              //because it will now be using a valid default template.
              setSiteHasInvalidSection(false)
              setSectionContainsErrors(false)
            }
            const sectionTemplate = JSON.stringify(sectionTemplates[opt.label])
            setSectionTypeOpt(opt)
            updateSection({
              ...section,
              sectionType: opt.value as SectionType,
              sectionConfig: sectionTemplate
            })
          }
        }}/>
      { moveSection && <IconButton
        aria-label="Move this section before the previous one"
        className="ms-2"
        disabled={readOnly || siteHasInvalidSection}
        icon={faChevronUp}
        variant="light"
        onClick={() => moveSection('up')}
      /> }
      { moveSection && <IconButton
        aria-label="Move this section after the next one"
        className="ms-2"
        disabled={readOnly || siteHasInvalidSection}
        icon={faChevronDown}
        variant="light"
        onClick={() => moveSection('down')}
      /> }
      { removeSection && <IconButton
        aria-label="Delete this section"
        className="ms-2"
        disabled={readOnly || (siteHasInvalidSection && !sectionContainsErrors)}
        icon={faTimes}
        variant="light"
        onClick={() => removeSection()}
      /> }
    </div>
    <textarea value={editorValue} style={{ height: 'calc(100% - 2em)', width: '100%', minHeight: '300px' }}
      disabled={readOnly || (siteHasInvalidSection && !sectionContainsErrors)}
      className={classNames('w-100 flex-grow-1 form-control font-monospace',
        { 'is-invalid': sectionContainsErrors })}
      onChange={e => {
        handleEditorChange(e.target.value)
      }}/>
  </>
}

export default HtmlSectionEditor
