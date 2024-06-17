import React, { useEffect, useId, useState } from 'react'
import { FaqQuestion, HtmlSection, SectionConfig, SectionType, socialMediaSites, StepConfig } from '@juniper/ui-core'
import Select from 'react-select'
import { Button, IconButton } from 'components/forms/Button'
import { faChevronDown, faChevronUp, faPlus, faTimes } from '@fortawesome/free-solid-svg-icons'
import { sectionTemplates } from './sectionTemplates'
import classNames from 'classnames'
import { TextInput } from '../../components/forms/TextInput'
import { Textarea } from '../../components/forms/Textarea'
import { Checkbox } from '../../components/forms/Checkbox'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { getMediaUrl, SiteMediaMetadata } from '../../api/api'
import useReactSingleSelect from '../../util/react-select-utils'
import { ImageConfig, MediaConfig } from '@juniper/ui-core/build/participant/landing/ConfiguredMedia'
import { ButtonConfig } from '@juniper/ui-core/build/participant/landing/ConfiguredButton'
import { PortalEnvContext } from '../PortalRouter'


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

const BUTTON_TYPES = [
  { label: 'Internal Link', value: 'internalLink' },
  { label: 'External Link', value: 'externalLink' },
  { label: 'Join Study', value: 'join' },
  { label: 'Join Mailing List', value: 'mailingList' }
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

const StyleOptions = ({ section, updateSection }: {
  section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const contentId = useId()
  const targetSelector = `#${contentId}`
  const imagePositionOptions = [{ label: 'Left', value: 'left' }, { label: 'Right', value: 'right' }]
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
      <div className="collapse hide rounded-3 mb-2" id={contentId}
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
        { Object.hasOwnProperty.call(config, 'image') && <div className='mt-2'><Checkbox label={'Full Width'}
          checked={config.fullWidth as boolean == undefined ? false : config.fullWidth as boolean}
          onChange={value => {
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, fullWidth: value }) })
          }}/></div>}
        { Object.hasOwnProperty.call(config, 'image') &&
          <div className='mt-2'><Select options={imagePositionOptions}
            value={config.imagePosition ? imagePositionOptions.find(opt => opt.value === config.imagePosition)
              : undefined}
            onChange={opt => {
              updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, imagePosition: opt?.value }) })
            }}/></div>}
      </div>
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
        return (
          <div key={handleKey} className="d-flex align-items-center mb-2">
            <div className="col-3">
              <label className='fw-bold'>{site.label}</label>
            </div>
            <div className="col d-flex align-items-right col">
              <TextInput value={config[handleKey] as string} placeholder={`Enter ${site.label} handle. ` +
                  'Leave blank to hide'} onChange={value => {
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, [handleKey]: value }) })
              }}/>
            </div>
          </div>
        )
      })}
    </div>
  )
}

const ImageSelector = ({ portalEnvContext, imageList, image, onChange }: {
  portalEnvContext: PortalEnvContext,
  imageList: SiteMediaMetadata[], image: ImageConfig, onChange: (image: SiteMediaMetadata) => void
}) => {
  const initial = imageList.find(media => media.cleanFileName === image.cleanFileName)
  const [, setSelectedImage] = useState<SiteMediaMetadata | undefined>(initial)

  const imageOptionLabel = (image: SiteMediaMetadata, portalShortcode: string) => <div>
    {image.cleanFileName} <img style={{ maxHeight: '1.5em' }}
      src={getMediaUrl(portalShortcode, image!.cleanFileName, image!.version)}/>
  </div>

  const {
    onChange: imageOnChange, options, selectedOption, selectInputId
  } = useReactSingleSelect(
    imageList,
    (media: SiteMediaMetadata) => ({ label: imageOptionLabel(media, portalEnvContext.portal.shortcode), value: media }),
    setSelectedImage,
    initial
  )

  return (
    <div>
      <Select
        placeholder={'Select an image'}
        isSearchable={false}
        inputId={selectInputId}
        options={options}
        value={selectedOption}
        onChange={opt => {
          if (opt != undefined) {
            imageOnChange(opt)
            onChange(opt.value)
          }
        }}
      />
    </div>
  )
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

  return (
    <div>
      {hasTitle && <TitleEditor section={section} updateSection={updateSection}/>}
      {hasBlurb && <BlurbEditor section={section} updateSection={updateSection}/>}
      {hasImage && <ImageEditor portalEnvContext={portalEnvContext} section={section}
        updateSection={updateSection} siteMediaList={siteMediaList}/>}
      <StyleOptions section={section} updateSection={updateSection}/>
      {hasSteps && <StepEditor portalEnvContext={portalEnvContext} section={section}
        updateSection={updateSection} siteMediaList={siteMediaList}/>}
      {hasQuestions && <QuestionEditor section={section} updateSection={updateSection}/>}
      {hasLogos && <LogoEditor portalEnvContext={portalEnvContext} section={section}
        updateSection={updateSection} siteMediaList={siteMediaList}/>}
      {hasButtons && <ButtonEditor section={section} updateSection={updateSection}/>}
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
  SOCIAL_MEDIA: SocialMediaSectionEditor,
  STEP_OVERVIEW: DynamicSectionEditor,
  PHOTO_BLURB_GRID: DynamicSectionEditor,
  PARTICIPATION_DETAIL: DynamicSectionEditor,
  RAW_HTML: undefined,
  LINK_SECTIONS_FOOTER: DynamicSectionEditor,
  BANNER_IMAGE: DynamicSectionEditor
}


/* UPDATED COMPONENTS DOWN HERE */

const TitleEditor = ({ section, updateSection }: {
  section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  return (
    <TextInput className="mb-2" label="Title" value={config.title as string} onChange={value => {
      const parsed = JSON.parse(section.sectionConfig || '{}')
      updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, title: value }) })
    }}/>
  )
}

const BlurbEditor = ({ section, updateSection }: {
  section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  return (
    <Textarea rows={3} className="mb-2" label="Blurb" value={config.blurb as string} onChange={value => {
      const parsed = JSON.parse(section.sectionConfig || '{}')
      updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, blurb: value }) })
    }}/>
  )
}

const QuestionEditor = ({ section, updateSection }: {
  section: HtmlSection, updateSection: (section: HtmlSection) => void
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const questions = config.questions as FaqQuestion[] || []
  const faqContentId = useId()
  const faqTargetSelector = `#${faqContentId}`
  return (<div>
    <div className="pb-1">
      <button
        aria-controls={faqTargetSelector}
        aria-expanded="true"
        className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
        data-bs-target={faqTargetSelector}
        data-bs-toggle="collapse"
      >
        <span className={'form-label fw-semibold mb-0'}>Questions ({questions.length})</span>
        <span className="text-center px-2">
          <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
          <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
        </span>
      </button>
    </div>
    <div className="collapse hide rounded-3 mb-2" id={faqContentId}
      style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
      {questions.map((question, i) => {
        return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
          <div className="d-flex justify-content-between align-items-center">
            <span className="h5">Question {i + 1}</span>
            <div role="button" className="d-flex justify-content-end">
              <FontAwesomeIcon icon={faTimes} className={'text-danger'} onClick={() => {
                const parsed = JSON.parse(section.sectionConfig!)
                const newQuestions = [...config.questions as FaqQuestion[]]
                newQuestions.splice(i, 1)
                updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, questions: newQuestions }) })
              }}/>
            </div>
          </div>
          <TextInput label="Question" value={question.question} onChange={value => {
            const parsed = JSON.parse(section.sectionConfig!)
            const newQuestions = [...config.questions as FaqQuestion[]]
            newQuestions[i].question = value
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, questions: newQuestions }) })
          }}/>
          <Textarea rows={3} label="Answer" value={question.answer} onChange={value => {
            const parsed = JSON.parse(section.sectionConfig!)
            const newQuestions = [...config.questions as FaqQuestion[]]
            newQuestions[i].answer = value
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, questions: newQuestions }) })
          }}/>
        </div>
      })}
      <Button onClick={() => {
        const parsed = JSON.parse(section.sectionConfig!)
        const newQuestions = [...config.questions as FaqQuestion[]]
        newQuestions.push({ question: '', answer: '' })
        updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, questions: newQuestions }) })
      }}><FontAwesomeIcon icon={faPlus}/> Add Question</Button>
    </div>
  </div>
  )
}

const StepEditor = ({ portalEnvContext, section, updateSection, siteMediaList }: {
  portalEnvContext: PortalEnvContext,
  section: HtmlSection, updateSection: (section: HtmlSection) => void, siteMediaList: SiteMediaMetadata[]
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const steps = config.steps as StepConfig[] || []
  const stepContentId = useId()
  const stepTargetSelector = `#${stepContentId}`
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={stepTargetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={stepTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Steps ({steps.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={stepContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div className='mb-2'><Checkbox label={'Show Step Numbers'}
          checked={config.showStepNumbers as boolean == undefined ? true : config.showStepNumbers as boolean}
          onChange={value => {
            const parsed = JSON.parse(section.sectionConfig || '{}')
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, showStepNumbers: value }) })
          }}/></div>
        <div>
          {steps.map((step, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div className="d-flex justify-content-between align-items-center">
                <span className="h5">Step {i + 1}</span>
                <div role="button" className="d-flex justify-content-end">
                  <FontAwesomeIcon icon={faTimes} className={'text-danger'} onClick={() => {
                    const parsed = JSON.parse(section.sectionConfig!)
                    const newSteps = [...config.steps as StepConfig[]]
                    newSteps.splice(i, 1)
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
                  }}/></div>
              </div>
              <div>
                <label className='form-label fw-semibold m-0'>Image</label>
                <ImageSelector portalEnvContext={portalEnvContext}
                  imageList={siteMediaList} image={step.image as ImageConfig} onChange={image => {
                    const parsed = JSON.parse(section.sectionConfig!)
                    const newSteps = [...config.steps as StepConfig[]]
                    newSteps[i].image = image
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
                  }}/>
                <TextInput label="Duration" value={step.duration} onChange={value => {
                  const parsed = JSON.parse(section.sectionConfig!)
                  const newSteps = [...config.steps as StepConfig[]]
                  newSteps[i].duration = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
                }}/>
                <Textarea rows={2} label="Blurb" value={step.blurb} onChange={value => {
                  const parsed = JSON.parse(section.sectionConfig!)
                  const newSteps = [...config.steps as StepConfig[]]
                  newSteps[i].blurb = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
                }}/>
              </div>
            </div>
          })}
        </div>
        <Button onClick={() => {
          const parsed = JSON.parse(section.sectionConfig!)
          const newSteps = [...config.steps as StepConfig[]]
          newSteps.push({ image: { cleanFileName: '', version: 1 }, duration: '', blurb: '' })
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, steps: newSteps }) })
        }}><FontAwesomeIcon icon={faPlus}/> Add Step</Button>
      </div>
    </div>
  )
}

const ImageEditor = ({ portalEnvContext, section, updateSection, siteMediaList }: {
  portalEnvContext: PortalEnvContext,
  section: HtmlSection, updateSection: (section: HtmlSection) => void, siteMediaList: SiteMediaMetadata[]
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  return (
    <div>
      <label className='form-label fw-semibold'>Image</label>
      <ImageSelector portalEnvContext={portalEnvContext}
        imageList={siteMediaList} image={config.image as ImageConfig} onChange={image => {
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, image }) })
        }}/>
    </div>
  )
}

const LogoEditor = ({ portalEnvContext, section, updateSection, siteMediaList }: {
  portalEnvContext: PortalEnvContext,
  section: HtmlSection, updateSection: (section: HtmlSection) => void, siteMediaList: SiteMediaMetadata[]
}) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const logos = config.logos as ImageConfig[] || []

  const logosContentId = useId()
  const logosTargetSelector = `#${logosContentId}`
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={logosTargetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={logosTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Logos ({logos.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={logosContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div>
          {logos.map((logo, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div className="d-flex justify-content-between align-items-center">
                <span className="h5">Logo {i + 1}</span>
                <div role="button" className="d-flex justify-content-end">
                  <FontAwesomeIcon icon={faTimes} className={'text-danger'} onClick={() => {
                    const parsed = JSON.parse(section.sectionConfig!)
                    const newLogos = [...config.logos as MediaConfig[]]
                    newLogos.splice(i, 1)
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
                  }}/></div>
              </div>
              <div>
                <label className='form-label fw-semibold m-0'>Image</label>
                <ImageSelector portalEnvContext={portalEnvContext}
                  imageList={siteMediaList} image={logo as ImageConfig} onChange={image => {
                    const parsed = JSON.parse(section.sectionConfig || '{}')
                    const newLogos = [...config.logos as MediaConfig[]]
                    newLogos[i] = {
                      cleanFileName: image.cleanFileName,
                      version: image.version
                    }
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
                  }}/>
                <TextInput label="Alt Text" value={logo.alt} onChange={value => {
                  const parsed = JSON.parse(section.sectionConfig || '{}')
                  const newLogos = [...config.logos as MediaConfig[]]
                  newLogos[i].alt = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
                }}/>

                <TextInput label="Link" value={logo.link} onChange={value => {
                  const parsed = JSON.parse(section.sectionConfig || '{}')
                  const newLogos = [...config.logos as ImageConfig[]]
                  newLogos[i].link = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
                }}/>

              </div>
            </div>
          })}
        </div>
        <Button onClick={() => {
          const parsed = JSON.parse(section.sectionConfig!)
          const newLogos = [...logos]
          newLogos.push({ cleanFileName: '', version: 1 })
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, logos: newLogos }) })
        }}><FontAwesomeIcon icon={faPlus}/> Add Logo</Button>
      </div>
    </div>
  )
}

const PhotoBioEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
    }) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const photos = config.photos as MediaConfig[] || []

  const photosContentId = useId()
  const photosTargetSelector = `#${photosContentId}`
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={photosTargetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={photosTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Photos ({photos.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={photosContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div>
            not yet implemented
        </div>
      </div>
    </div>
  )
}

const ButtonEditor = ({ section, updateSection }: {
    section: HtmlSection, updateSection: (section: HtmlSection) => void
    }) => {
  const config = JSON.parse(section.sectionConfig || '{}') as SectionConfig
  const buttons = config.buttons as ButtonConfig[] || []
  const buttonsContentId = useId()
  const buttonsTargetSelector = `#${buttonsContentId}`
  return (
    <div>
      <div className="pb-1">
        <button
          aria-controls={buttonsTargetSelector}
          aria-expanded="true"
          className={classNames('btn w-100 py-2 px-0 d-flex text-decoration-none')}
          data-bs-target={buttonsTargetSelector}
          data-bs-toggle="collapse"
        >
          <span className={'form-label fw-semibold mb-0'}>Buttons ({buttons.length})</span>
          <span className="text-center px-2">
            <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
            <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
          </span>
        </button>
      </div>
      <div className="collapse hide rounded-3 mb-2" id={buttonsContentId}
        style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
        <div>
          {buttons.map((button, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div className="d-flex justify-content-between align-items-center">
                <span className="h5">Button {i + 1}</span>
                <div role="button" className="d-flex justify-content-end">
                  <FontAwesomeIcon icon={faTimes} className={'text-danger'} onClick={() => {
                    const parsed = JSON.parse(section.sectionConfig!)
                    const newButtons = [...config.buttons as ButtonConfig[]]
                    newButtons.splice(i, 1)
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, buttons: newButtons }) })
                  }}/></div>
              </div>
              <div>
                <label className='form-label fw-semibold m-0'>Button Type</label>
                <Select className='w-100' options={BUTTON_TYPES} value={BUTTON_TYPES[
                  BUTTON_TYPES.findIndex(opt => opt.value === button.type)
                ]} aria-label={'Select section type'}
                onChange={opt => {
                  if (opt != undefined) {
                    const parsed = JSON.parse(section.sectionConfig || '{}')
                    const newButtons = [...config.buttons as ButtonConfig[]]
                    newButtons[i].type = ButtonTypeTemplates[opt.value].type
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, buttons: newButtons }) })
                  }
                }}/>
                <TextInput label="Button Text" value={button.text} onChange={value => {
                  const parsed = JSON.parse(section.sectionConfig || '{}')
                  const newButtons = [...config.buttons as ButtonConfig[]]
                  newButtons[i].text = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, buttons: newButtons }) })
                }}/>
                {/* @ts-ignore */}
                {button.type === 'externalLink' || button.type === 'internalLink' &&
                  <TextInput label="Button Link" value={button.href} onChange={value => {
                    const parsed = JSON.parse(section.sectionConfig || '{}')
                    const newButtons = [...config.buttons as ButtonConfig[]]
                    // @ts-ignore
                    newButtons[i].href = value
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, buttons: newButtons }) })
                  }}/>}
                {button.type === 'join' &&
                  <TextInput label="Study Shortcode" value={button.studyShortcode} onChange={value => {
                    const parsed = JSON.parse(section.sectionConfig || '{}')
                    const newButtons = [...config.buttons as ButtonConfig[]]
                    // @ts-ignore
                    newButtons[i].studyShortcode = value
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, buttons: newButtons }) })
                  }}/>}
              </div>
            </div>
          })}
        </div>
        <Button onClick={() => {
          const parsed = JSON.parse(section.sectionConfig!)
          const newButtons = [...buttons]
          newButtons.push({ type: undefined, text: '', href: '' })
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...parsed, buttons: newButtons }) })
        }}><FontAwesomeIcon icon={faPlus}/> Add Button
        </Button>
      </div>
    </div>
  )
}

const ButtonTypeTemplates: Record<string, ButtonConfig> = {
  'internalLink': {
    type: 'internalLink',
    text: '',
    href: ''
  },
  'join': {
    type: 'join',
    text: 'Join Study',
    studyShortcode: ''
  },
  'mailingList': {
    type: 'mailingList',
    text: 'Join Mailing List'
  },
  'externalLink': {
    type: undefined,
    text: '',
    href: ''
  }
}

