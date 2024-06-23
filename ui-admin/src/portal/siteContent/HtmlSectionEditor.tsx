import React, { useEffect, useState } from 'react'
import {
  HtmlSection,
  SectionType
} from '@juniper/ui-core'
import Select from 'react-select'
import { IconButton } from 'components/forms/Button'
import { faChevronDown, faChevronUp, faTimes } from '@fortawesome/free-solid-svg-icons'
import { sectionTemplates } from './sectionTemplates'
import classNames from 'classnames'
import { SiteMediaMetadata } from 'api/api'
import { PortalEnvContext } from '../PortalRouter'
import { ButtonEditor } from './designer/components/ButtonEditor'
import { FrequentlyAskedQuestionEditor } from './designer/components/FrequentlyAskedQuestionEditor'
import { TitleEditor } from './designer/components/TitleEditor'
import { BlurbEditor } from './designer/components/BlurbEditor'
import { ImageEditor } from './designer/components/ImageEditor'
import { StyleEditor } from './designer/components/StyleEditor'
import { ParticipationStepsEditor } from './designer/components/ParticipantStepsEditor'
import { LogoEditor } from './designer/components/LogoEditor'
import { PhotoBioEditor } from './designer/components/PhotoBioEditor'
import { SocialMediaEditor } from './designer/components/SocialMediaEditor'

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

    {SectionEditorComponent && !useJsonEditor ? (
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

const DynamicSectionEditor = ({ portalEnvContext, section, updateSection, siteMediaList }: {
  portalEnvContext: PortalEnvContext,
  section: HtmlSection, updateSection: (section: HtmlSection) => void, siteMediaList: SiteMediaMetadata[]
}) => {
  const sectionType = section.sectionType
  const sectionTypeConfig = sectionTemplates[sectionType]
  const hasTitle = Object.hasOwnProperty.call(sectionTypeConfig, 'title')
  const hasBlurb = Object.hasOwnProperty.call(sectionTypeConfig, 'blurb')
  const hasSteps = Object.hasOwnProperty.call(sectionTypeConfig, 'steps')
  const hasQuestions = Object.hasOwnProperty.call(sectionTypeConfig, 'questions')
  const hasImage = Object.hasOwnProperty.call(sectionTypeConfig, 'image')
  const hasLogos = Object.hasOwnProperty.call(sectionTypeConfig, 'logos')
  const hasButtons = Object.hasOwnProperty.call(sectionTypeConfig, 'buttons')
  const hasSubGrids = Object.hasOwnProperty.call(sectionTypeConfig, 'subGrids')

  return (
    <div>
      {hasTitle &&
          <TitleEditor section={section} updateSection={updateSection}/>}
      {hasBlurb &&
          <BlurbEditor section={section} updateSection={updateSection}/>}
      {hasImage &&
          <ImageEditor portalEnvContext={portalEnvContext} section={section}
            updateSection={updateSection} siteMediaList={siteMediaList}/>}
      {/* All sections have a style editor */}
      <StyleEditor section={section} updateSection={updateSection}/>
      {hasSteps &&
          <ParticipationStepsEditor portalEnvContext={portalEnvContext} section={section}
            updateSection={updateSection} siteMediaList={siteMediaList}/>}
      {hasQuestions &&
          <FrequentlyAskedQuestionEditor section={section} updateSection={updateSection}/>}
      {hasLogos &&
          <LogoEditor portalEnvContext={portalEnvContext} section={section}
            updateSection={updateSection} siteMediaList={siteMediaList}/>}
      {hasButtons &&
          <ButtonEditor
            section={section} updateSection={updateSection}/>}
      {hasSubGrids &&
          <PhotoBioEditor portalEnvContext={portalEnvContext} mediaList={siteMediaList}
            section={section} updateSection={updateSection}/>}
    </div>
  )
}

export default HtmlSectionEditor

type SectionEditorComponentType = ({ section, updateSection }: {
  portalEnvContext: PortalEnvContext
  siteMediaList: SiteMediaMetadata[]
  section: HtmlSection
  updateSection: (section: HtmlSection) => void
}) => JSX.Element

const SectionEditorComponents: Record<SectionType, SectionEditorComponentType | undefined> = {
  FAQ: DynamicSectionEditor,
  HERO_CENTERED: DynamicSectionEditor,
  HERO_WITH_IMAGE: DynamicSectionEditor,
  SOCIAL_MEDIA: SocialMediaEditor,
  STEP_OVERVIEW: DynamicSectionEditor,
  PHOTO_BLURB_GRID: DynamicSectionEditor,
  PARTICIPATION_DETAIL: DynamicSectionEditor,
  RAW_HTML: undefined,
  LINK_SECTIONS_FOOTER: DynamicSectionEditor,
  BANNER_IMAGE: DynamicSectionEditor
}
