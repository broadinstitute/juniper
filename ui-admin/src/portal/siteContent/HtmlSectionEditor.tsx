import React, { useEffect, useState } from 'react'
import {
  HtmlSection, SectionConfig,
  SectionType,
  validateStepOverviewTemplateConfig
} from '@juniper/ui-core'
import Select from 'react-select'
import { IconButton } from 'components/forms/Button'
import { faChevronDown, faChevronUp, faTimes } from '@fortawesome/free-solid-svg-icons'
import { sectionTemplates } from './sectionTemplates'
import classNames from 'classnames'
import { ImageConfig } from '@juniper/ui-core/build/participant/landing/ConfiguredMedia'
import { TextInput } from '../../components/forms/TextInput'
import { Textarea } from '../../components/forms/Textarea'
import { Checkbox } from '../../components/forms/Checkbox'

const SECTION_TYPES = [
  { label: 'FAQ', value: 'FAQ' },
  { label: 'Hero (centered)', value: 'HERO_CENTERED' },
  { label: 'Hero (with image)', value: 'HERO_WITH_IMAGE' },
  { label: 'Social Media', value: 'SOCIAL_MEDIA' },
  { label: 'Step Overview', value: 'STEP_OVERVIEW' },
  { label: 'Photo Blurb Grid', value: 'PHOTO_BLURB_GRID' },
  { label: 'Participation Detail', value: 'PARTICIPATION_DETAIL' },
  { label: 'Raw HTML', value: 'RAW_HTML' },
  { label: 'Link Sections Footer', value: 'LINK_SECTIONS_FOOTER' },
  { label: 'Banner Image', value: 'BANNER_IMAGE' }
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
    {section.sectionType === 'STEP_OVERVIEW' ?
      <StepOverviewSectionEditor section={section} updateSection={updateSection}/>

      : <textarea value={editorValue} style={{ height: 'calc(100% - 2em)', width: '100%', minHeight: '300px' }}
        disabled={readOnly || (siteHasInvalidSection && !sectionContainsErrors)}
        className={classNames('w-100 flex-grow-1 form-control font-monospace',
          { 'is-invalid': sectionContainsErrors })}
        onChange={e => {
          handleEditorChange(e.target.value)
        }}/>}
  </>
}

const StepOverviewSectionEditor = ({ section, updateSection }: {
  section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = validateStepOverviewTemplateConfig(JSON.parse(section.sectionConfig || '{}') as SectionConfig)
  return (
    <div>
      <div className="d-flex row g-0">
        <TextInput className="mb-2" label="Title" value={config.title} onChange={value => {
          const parsed = JSON.parse(section.sectionConfig || '{}')
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, title: value }) })
        }}/>
        <Checkbox label={'Show Step Numbers'}
          checked={config.showStepNumbers == undefined ? true : config.showStepNumbers} onChange={value => {
            const parsed = JSON.parse(section.sectionConfig || '{}')
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, showStepNumbers: value }) })
          }}/>
        <div>
          {config.steps.map((step, i) => {
            return <div key={i} style={{ backgroundColor: '#eee', padding: '0.75rem' }} className="rounded-3 mb-2">
              <TextInput label="Image" value={(step.image as ImageConfig).cleanFileName}
                onChange={value => {
                  const parsed = JSON.parse(section.sectionConfig!)
                  const newSteps = [...config.steps]
                  newSteps[i].image = { cleanFileName: value, version: 1 } //todo hardcoded version
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
                }}/>
              <TextInput label="Duration" value={step.duration} onChange={value => {
                const parsed = JSON.parse(section.sectionConfig!)
                const newSteps = [...config.steps]
                newSteps[i].duration = value
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
              }}/>
              <Textarea rows={2} label="Blurb" value={step.blurb} onChange={value => {
                const parsed = JSON.parse(section.sectionConfig!)
                const newSteps = [...config.steps]
                newSteps[i].blurb = value
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
              }}/>
            </div>
          })}
        </div>
        Add step
        <button onClick={() => {
          const parsed = JSON.parse(section.sectionConfig!)
          const newSteps = [...config.steps]
          newSteps.push({ image: { cleanFileName: '', version: 1 }, duration: '', blurb: '' })
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
        }}>+</button>
      </div>
    </div>
  )
}

const ImageSelector = ({ image, onChange }: { image: ImageConfig, onChange: (image: ImageConfig) => void }) => {
  return (
    <div>
      <TextInput label="Image" value={image.cleanFileName} onChange={value => {
        onChange({ ...image, cleanFileName: value })
      }}/>
      <TextInput label="Version" value={image.version} onChange={value => {
        onChange({ ...image, version: parseInt(value) })
      }}/>
    </div>
  )
}

export default HtmlSectionEditor
