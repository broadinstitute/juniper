import { PortalEnvContext } from '../../../PortalRouter'
import { HtmlSection, ImageConfig, SectionConfig, StepConfig } from '@juniper/ui-core'
import { SiteMediaMetadata } from 'api/api'
import React, { useId } from 'react'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus } from '@fortawesome/free-solid-svg-icons'
import { Checkbox } from 'components/forms/Checkbox'
import { TextInput } from 'components/forms/TextInput'
import { Textarea } from 'components/forms/Textarea'
import { Button } from 'components/forms/Button'
import { ImageSelector } from './ImageSelector'
import { ListElementController } from './ListElementController'

/**
 *
 */
export const ParticipationStepsEditor = ({ portalEnvContext, section, updateSection, siteMediaList }: {
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
            updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, showStepNumbers: value }) })
          }}/></div>
        <div>
          {steps.map((step, i) => {
            return <div key={i} style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
              <div className="d-flex justify-content-between align-items-center">
                <span className="h5">Edit step</span>
                <ListElementController<StepConfig>
                  items={config.steps as StepConfig[]}
                  updateItems={newSteps => {
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, steps: newSteps }) })
                  }}
                  index={i}
                />
              </div>
              <div>
                <label className='form-label fw-semibold m-0'>Image</label>
                <ImageSelector portalEnvContext={portalEnvContext}
                  imageList={siteMediaList} image={step.image as ImageConfig} onChange={image => {
                    const newSteps = [...config.steps as StepConfig[]]
                    newSteps[i].image = image
                    updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, steps: newSteps }) })
                  }}/>
                <TextInput label="Duration" className="mb-2" value={step.duration} onChange={value => {
                  const newSteps = [...config.steps as StepConfig[]]
                  newSteps[i].duration = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, steps: newSteps }) })
                }}/>
                <Textarea rows={2} label="Blurb" className="mb-2" value={step.blurb} onChange={value => {
                  const newSteps = [...config.steps as StepConfig[]]
                  newSteps[i].blurb = value
                  updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, steps: newSteps }) })
                }}/>
              </div>
            </div>
          })}
        </div>
        <Button onClick={() => {
          const newSteps = [...config.steps as StepConfig[]]
          newSteps.push({ image: { cleanFileName: '', version: 1 }, duration: '', blurb: '' })
          updateSection({ ...section, sectionConfig: JSON.stringify({ ...config, steps: newSteps }) })
        }}><FontAwesomeIcon icon={faPlus}/> Add Step</Button>
      </div>
    </div>
  )
}
