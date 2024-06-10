import React, { useEffect, useId, useState } from 'react'
import {
  HtmlSection, SectionConfig,
  SectionType, socialMediaSites, validateFrequentlyAskedQuestionsConfig,
  validateStepOverviewTemplateConfig
} from '@juniper/ui-core'
import Select from 'react-select'
import { Button, IconButton } from 'components/forms/Button'
import { faChevronDown, faChevronUp, faPlus, faTimes } from '@fortawesome/free-solid-svg-icons'
import { sectionTemplates } from './sectionTemplates'
import classNames from 'classnames'
import { ImageConfig } from '@juniper/ui-core/build/participant/landing/ConfiguredMedia'
import { TextInput } from '../../components/forms/TextInput'
import { Textarea } from '../../components/forms/Textarea'
import { Checkbox } from '../../components/forms/Checkbox'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

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
      : section.sectionType === 'SOCIAL_MEDIA' ?
        <SocialMediaSectionEditor section={section} updateSection={updateSection}/>
        : section.sectionType === 'FAQ' ?
          <FrequentlyAskedQuestionsSectionEditor section={section} updateSection={updateSection}/>
          : <textarea value={editorValue} style={{ height: 'calc(100% - 2em)', width: '100%', minHeight: '300px' }}
            disabled={readOnly || (siteHasInvalidSection && !sectionContainsErrors)}
            className={classNames('w-100 flex-grow-1 form-control font-monospace',
              { 'is-invalid': sectionContainsErrors })}
            onChange={e => {
              handleEditorChange(e.target.value)
            }}/>
    }
  </>
}

const StepOverviewSectionEditor = ({ section, updateSection }: {
  section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = validateStepOverviewTemplateConfig(JSON.parse(section.sectionConfig || '{}') as SectionConfig)
  const stepContentId = useId()
  const stepTargetSelector = `#${stepContentId}`
  return (
    <div>
      <div className="d-flex row g-0">
        <TextInput className="mb-2" label="Title" value={config.title} onChange={value => {
          const parsed = JSON.parse(section.sectionConfig || '{}')
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, title: value }) })
        }}/>
        {/*<TextInput label="Background Color" value={config.background} onChange={value => {*/}
        <StyleOptions section={section} updateSection={updateSection}/>
        <div>
          <div className="pb-1">
            <button
              aria-controls={stepTargetSelector}
              aria-expanded="true"
              className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
              data-bs-target={stepTargetSelector}
              data-bs-toggle="collapse"
            >
              <span className={'form-label fw-semibold mb-0'}>Steps</span>
              <span className="text-center px-2">
                <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
                <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
              </span>
            </button>
          </div>
          <div className="collapse show rounded-3 mb-2" id={stepContentId}
            style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
            <Checkbox className="mb-2" label={'Show Step Numbers'}
              checked={config.showStepNumbers == undefined ? true : config.showStepNumbers} onChange={value => {
                const parsed = JSON.parse(section.sectionConfig || '{}')
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, showStepNumbers: value }) })
              }}/>
            <div>
              {config.steps.map((step, i) => {
                return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
                  <div className="d-flex justify-content-between align-items-center">
                    <span className="h5">Step {i + 1}</span>
                    <div role="button" className="d-flex justify-content-end">
                      <FontAwesomeIcon icon={faTimes} className={'text-danger'} onClick={() => {
                        const parsed = JSON.parse(section.sectionConfig!)
                        const newSteps = [...config.steps]
                        newSteps.splice(i, 1)
                        updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
                      }}/></div>
                  </div>
                  <div>
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
                </div>
              })}
            </div>
            <Button onClick={() => {
              const parsed = JSON.parse(section.sectionConfig!)
              const newSteps = [...config.steps]
              newSteps.push({ image: { cleanFileName: '', version: 1 }, duration: '', blurb: '' })
              updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
            }}><FontAwesomeIcon icon={faPlus}/> Add Step</Button>
          </div>
        </div>
      </div>
    </div>
  )
}

const StyleOptions = ({ section, updateSection }: {
  section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  console.log(config)
  const contentId = useId()
  const targetSelector = `#${contentId}`
  // @ts-ignore
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={targetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={targetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Style Options</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse show rounded-3 mb-2" id={contentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <TextInput label="Background Color" value={config.background as string}
          placeholder={'Enter a value to override default'}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, background: value }) })
          }}/>
        <TextInput label="Text Color" value={config.color as string}
          placeholder={'Enter a value to override default'}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, color: value }) })
          }}/>
      </div>
    </div>
  )
}

const FrequentlyAskedQuestionsSectionEditor = ({ section, updateSection }: {
  section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = validateFrequentlyAskedQuestionsConfig(JSON.parse(section.sectionConfig || '{}') as SectionConfig)
  return (
    <div className="d-flex row g-0">
      <TextInput className="mb-2" label="Title" value={config.title} onChange={value => {
        const parsed = JSON.parse(section.sectionConfig || '{}')
        updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, title: value }) })
      }}/>
      <Textarea className="mb-2" label="Blurb" value={config.blurb} onChange={value => {
        const parsed = JSON.parse(section.sectionConfig || '{}')
        updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, blurb: value }) })
      }}/>
      {config.questions.map((question, i) => {
        return <div key={i} style={{ backgroundColor: '#eee', padding: '0.75rem' }} className="rounded-3 mb-2">
          <div role="button" className="d-flex justify-content-end">
            <FontAwesomeIcon icon={faTimes} className={'text-danger'} onClick={() => {
              const parsed = JSON.parse(section.sectionConfig!)
              const newQuestions = [...config.questions]
              newQuestions.splice(i, 1)
              updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, questions: newQuestions }) })
            }}/></div>
          <TextInput label="Question" value={question.question} onChange={value => {
            const parsed = JSON.parse(section.sectionConfig!)
            const newQuestions = [...config.questions]
            newQuestions[i].question = value
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, questions: newQuestions }) })
          }}/>
          <Textarea rows={2} label="Answer" value={question.answer} onChange={value => {
            const parsed = JSON.parse(section.sectionConfig!)
            const newQuestions = [...config.questions]
            newQuestions[i].answer = value
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, questions: newQuestions }) })
          }}/>
        </div>
      })}
      <Button onClick={() => {
        const parsed = JSON.parse(section.sectionConfig!)
        const newQuestions = [...config.questions]
        newQuestions.push({ question: '', answer: '' })
        updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, questions: newQuestions }) })
      }}><FontAwesomeIcon icon={faPlus}/> Add Question</Button>
    </div>
  )
}

const SocialMediaSectionEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
    }) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  return (
    <div>
      {socialMediaSites.map(site => {
        const handleKey = `${site.label.toLowerCase()}Handle`
        const handle = config[handleKey]
        return (
          <div key={site.label} className="d-flex justify-content-between align-items-center">
            <span>{site.label}</span>
            <TextInput value={'handle'} onChange={value => {
              updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, [handleKey]: value }) })
            }}/>
          </div>
        )
      })}
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
