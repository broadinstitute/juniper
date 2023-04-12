import _ from 'lodash'
import React from 'react'

import { SectionConfig } from 'api/api'
import { getSectionStyle } from 'util/styleUtils'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalArray, requireOptionalString, requirePlainObject, requireString } from 'util/validationUtils'

import ConfiguredButton, { ButtonConfig, validateButtonConfig } from '../ConfiguredButton'
import ConfiguredImage, { ImageConfig, validateImageConfig } from '../ConfiguredImage'
import { InlineMarkdown } from '../Markdown'

import { TemplateComponentProps } from './templateUtils'

type StepConfig = {
  image: ImageConfig,
  duration: string,
  blurb: string
}

type StepOverviewTemplateConfig = {
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  steps: StepConfig[]
  title?: string, // large heading text
}

const validateStepConfig = (config: unknown): StepConfig => {
  const message = 'Invalid StepOverviewTemplateConfig: Invalid step'
  const configObj = requirePlainObject(config, message)
  const image = validateImageConfig(configObj.image)
  const duration = requireString(configObj, 'duration', message)
  const blurb = requireString(configObj, 'blurb', message)
  return { image, duration, blurb }
}

/** Validate that a section configuration object conforms to StepOverviewTemplateConfig */
const validateStepOverviewTemplateConfig = (config: SectionConfig): StepOverviewTemplateConfig => {
  const message = 'Invalid StepOverviewTemplateConfig'
  const buttons = requireOptionalArray(config, 'buttons', validateButtonConfig, message)
  const title = requireOptionalString(config, 'title', message)
  const steps = requireOptionalArray(config, 'steps', validateStepConfig, message)
  return { buttons, steps, title }
}

type StepOverviewTemplateProps = TemplateComponentProps<StepOverviewTemplateConfig>

/**
 * Template for rendering a step overview
 */
function StepOverviewTemplate(props: StepOverviewTemplateProps) {
  const { anchorRef, config } = props
  const { buttons, steps, title } = config

  const hasButtons = (buttons || []).length > 0

  // TODO: improve layout code for better flexing, especially with <> 4 steps
  return <div id={anchorRef} style={getSectionStyle(config)}>
    {!!title && (
      <h2 className="fs-1 fw-normal lh-sm text-center">
        <InlineMarkdown>{title}</InlineMarkdown>
      </h2>
    )}
    <div className="row mx-0">
      {
        _.map(steps, ({ image, duration, blurb }: StepConfig, i: number) => {
          return <div key={i} className="col-12 col-lg-3 d-flex flex-column align-items-center mt-4">
            <div className="w-75 d-flex flex-column align-items-center align-items-lg-start">
              <ConfiguredImage image={image} className="img-fluid p-3" style={{ maxWidth: '200px' }}/>
              <p className="text-uppercase fs-5 fw-semibold mb-0">Step {i + 1}</p>
              <p className="text-uppercase fs-6">{duration}</p>
              <p className="fs-4 mb-0">
                {blurb}
              </p>
            </div>
          </div>
        })
      }
    </div>
    {hasButtons && (
      <div className="d-grid gap-2 d-md-flex justify-content-center mt-4">
        {
          _.map(buttons, (button, i) => {
            return <ConfiguredButton key={i} config={button} className="px-4 me-md-2" />
          })
        }
      </div>
    )}
  </div>
}

export default withValidatedSectionConfig(validateStepOverviewTemplateConfig, StepOverviewTemplate)
