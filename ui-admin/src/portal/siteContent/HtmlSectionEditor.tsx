import React, { useEffect, useState } from 'react'
import {
  HtmlSection,
  SectionType
} from '@juniper/ui-core'
import Select from 'react-select'
import { IconButton } from 'components/forms/Button'
import { faChevronDown, faChevronUp, faCode, faTimes } from '@fortawesome/free-solid-svg-icons'
import { sectionTemplates } from './sectionTemplates'
import classNames from 'classnames'
import { SiteMediaMetadata } from 'api/api'
import { PortalEnvContext } from 'portal/PortalRouter'
import { SocialMediaEditor } from './designer/editors/SocialMediaEditor'
import { SectionDesigner } from './designer/SectionDesigner'

const SECTION_TYPES = [
  { label: 'FAQ', value: 'FAQ' },
  { label: 'Hero (centered)', value: 'HERO_CENTERED' },
  { label: 'Hero (with image)', value: 'HERO_WITH_IMAGE' },
  { label: 'Banner Image', value: 'BANNER_IMAGE' },
  { label: 'Participation Details', value: 'PARTICIPATION_DETAIL' },
  { label: 'Participation Step Overview', value: 'STEP_OVERVIEW' },
  { label: 'Photo Grid', value: 'PHOTO_BLURB_GRID' },
  { label: 'Social Media', value: 'SOCIAL_MEDIA' },
  { label: 'HTML', value: 'RAW_HTML' },
  { label: 'Footer', value: 'LINK_SECTIONS_FOOTER' }
]

/**
 * Returns an editor for an HtmlSection
 */
const HtmlSectionEditor = ({
  portalEnvContext,
  updateSection,
  removeSection,
  moveSection,
  section,
  siteMediaList,
  siteHasInvalidSection,
  setSiteHasInvalidSection,
  allowTypeChange,
  useJsonEditor = true,
  readOnly
}: {
  portalEnvContext: PortalEnvContext
  updateSection: (section: HtmlSection) => void
  removeSection?: () => void
  moveSection?: (direction: 'up' | 'down') => void
  section: HtmlSection
  siteMediaList: SiteMediaMetadata[]
  siteHasInvalidSection: boolean
  setSiteHasInvalidSection: (invalid: boolean) => void
  allowTypeChange: boolean
  useJsonEditor?: boolean
  readOnly: boolean
}) => {
  const [sectionContainsErrors, setSectionContainsErrors] = useState(false)
  const initial = SECTION_TYPES.find(sectionType => sectionType.value === section.sectionType)
  const [sectionTypeOpt, setSectionTypeOpt] = useState(initial)
  // Allows the user to switch a single section to the JSON view. Eventually we may want to get rid of
  // the full JSON Editor tab and just allow users to peek into the JSON for a single section.
  const [sectionUseJsonEditor, setSectionUseJsonEditor] = useState(useJsonEditor)

  const getSectionContent = (section: HtmlSection) => {
    if (section.sectionType === 'RAW_HTML') {
      return section.rawContent ?? ''
    } else {
      return JSON.stringify(JSON.parse(section?.sectionConfig ?? '{}'), null, 2)
    }
  }

  const [editorValue, setEditorValue] = useState(getSectionContent(section))

  useEffect(() => {
    setEditorValue(getSectionContent(section))
  }, [section.sectionConfig])

  const handleEditorChange = (newEditorValue: string) => {
    setEditorValue(newEditorValue)

    if (section.sectionType === 'RAW_HTML') {
      updateSection({ ...section, rawContent: newEditorValue, sectionConfig: undefined })
    } else {
      try {
        JSON.parse(newEditorValue)
        setSiteHasInvalidSection(false)
        setSectionContainsErrors(false)
        updateSection({ ...section, sectionConfig: newEditorValue, rawContent: undefined })
      } catch (e) {
        setSiteHasInvalidSection(true)
        setSectionContainsErrors(true)
        // Note that we do not call updateSection here, as that would result in an invalid preview being shown.
        // Instead, the preview will be based on the last valid config for this section.
      }
    }
  }

  const SectionEditorComponent = SectionEditorComponents[section.sectionType]

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
            const sectionTemplate = JSON.stringify(sectionTemplates[opt.value])
            setSectionTypeOpt(opt)
            updateSection({
              ...section,
              sectionType: opt.value as SectionType,
              sectionConfig: sectionTemplate
            })
          }
        }}/>
      { !useJsonEditor && <IconButton icon={faCode}
        aria-label={sectionUseJsonEditor ? 'Switch to designer' : 'Switch to JSON editor'}
        className="ms-2"
        variant="light"
        onClick={() => setSectionUseJsonEditor(!sectionUseJsonEditor)}
      /> }
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

    {SectionEditorComponent && !sectionUseJsonEditor ? (
      <SectionEditorComponent portalEnvContext={portalEnvContext}
        siteMediaList={siteMediaList} section={section} updateSection={updateSection} />
    ) : (
      <textarea
        value={editorValue}
        style={{ height: 'calc(100% - 2em)', width: '100%', minHeight: '300px' }}
        disabled={readOnly || (siteHasInvalidSection && !sectionContainsErrors)}
        className={classNames('w-100 flex-grow-1 form-control font-monospace', {
          'is-invalid': sectionContainsErrors
        })}
        onChange={e => {
          handleEditorChange(e.target.value)
        }}
      />
    )}
  </>
}

export default HtmlSectionEditor

type SectionEditorComponentType = ({ section, updateSection }: {
  portalEnvContext: PortalEnvContext
  siteMediaList: SiteMediaMetadata[]
  section: HtmlSection
  updateSection: (section: HtmlSection) => void
}) => JSX.Element

const SectionEditorComponents: Record<SectionType, SectionEditorComponentType | undefined> = {
  FAQ: SectionDesigner,
  HERO_CENTERED: SectionDesigner,
  HERO_WITH_IMAGE: SectionDesigner,
  SOCIAL_MEDIA: SocialMediaEditor,
  STEP_OVERVIEW: SectionDesigner,
  PHOTO_BLURB_GRID: SectionDesigner,
  PARTICIPATION_DETAIL: SectionDesigner,
  RAW_HTML: undefined,
  LINK_SECTIONS_FOOTER: SectionDesigner,
  BANNER_IMAGE: SectionDesigner
}
