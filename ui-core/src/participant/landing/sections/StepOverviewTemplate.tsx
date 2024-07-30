import _ from 'lodash'
import React from 'react'

import { SectionConfig } from '../../../types/landingPageConfig'
import { getSectionStyle } from '../../util/styleUtils'
import { withValidatedSectionConfig } from '../../util/withValidatedSectionConfig'
import {
  requireOptionalArray,
  requireOptionalBoolean,
  requireOptionalString,
  requirePlainObject,
  requireString
} from '../../util/validationUtils'

import ConfiguredButton, {
  ButtonConfig,
  buttonConfigProps,
  validateButtonConfig
} from '../ConfiguredButton'
import ConfiguredMedia, {
  MediaConfig,
  mediaConfigProps,
  validateMediaConfig
} from '../ConfiguredMedia'
import { InlineMarkdown } from '../Markdown'

import { TemplateComponentProps } from './templateUtils'
import { useApiContext } from '../../ApiProvider'
import classNames from 'classnames'
import {
  blurbProp,
  titleProp
} from './SectionProp'

export type StepConfig = {
  image: MediaConfig,
  duration: string,
  blurb: string,
}

export type StepOverviewTemplateConfig = {
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  steps: StepConfig[]
  showStepNumbers?: boolean, // whether to show step numbers, default true
  title?: string, // large heading text
}

export const stepOverviewTemplateConfigProps = [
  { name: 'buttons', subProps: buttonConfigProps, isArray: true },
  {
    name: 'steps', isArray: true, subProps: [
      { name: 'image', subProps: mediaConfigProps },
      { name: 'duration', translated: true },
      blurbProp
    ]
  },
  { name: 'showStepNumbers' },
  titleProp
]

const validateStepConfig = (config: unknown): StepConfig => {
  const message = 'Invalid StepOverviewTemplateConfig: Invalid step'
  const configObj = requirePlainObject(config, message)
  const image = validateMediaConfig(configObj.image)
  const duration = requireString(configObj, 'duration', message)
  const blurb = requireString(configObj, 'blurb', message)
  return { image, duration, blurb }
}

/** Validate that a section configuration object conforms to StepOverviewTemplateConfig */
export const validateStepOverviewTemplateConfig = (config: SectionConfig): StepOverviewTemplateConfig => {
  const message = 'Invalid StepOverviewTemplateConfig'
  const buttons = requireOptionalArray(config, 'buttons', validateButtonConfig, message)
  const title = requireOptionalString(config, 'title', message)
  const steps = requireOptionalArray(config, 'steps', validateStepConfig, message)
  const showStepNumbers = requireOptionalBoolean(config, 'showStepNumbers', message)
  return { buttons, steps, title, showStepNumbers }
}

type StepOverviewTemplateProps = TemplateComponentProps<StepOverviewTemplateConfig>

/**
 * Template for rendering a step overview
 */
function StepOverviewTemplate(props: StepOverviewTemplateProps) {
  const { anchorRef, config } = props
  const { buttons, steps, title, showStepNumbers = true } = config

  const hasButtons = (buttons || []).length > 0
  const { getImageUrl } = useApiContext()
  let lgWidthClass = 'col-lg-3'
  if (steps.length === 2) {
    lgWidthClass = 'col-lg-6'
  } else if (steps.length === 3) {
    lgWidthClass = 'col-lg-4'
  }
  return <div id={anchorRef} style={getSectionStyle(config, getImageUrl)}>
    {!!title && (
      <h2 className="fs-1 fw-normal lh-sm text-center">
        <InlineMarkdown>{title}</InlineMarkdown>
      </h2>
    )}
    <div className="row mx-0">
      {
        _.map(steps, ({ image, duration, blurb }: StepConfig, i: number) => {
          return <div key={i}
            className={classNames('col-12 d-flex flex-column align-items-center mt-4', lgWidthClass)}>
            <div className={classNames('w-75 d-flex flex-column align-items-center align-items-lg-start')}>
              <ConfiguredMedia media={image} className="img-fluid p-3" style={{ maxWidth: '200px' }}/>
              <div>{showStepNumbers && <p className="text-uppercase fs-5 fw-semibold mb-0">Step {i + 1}</p>}
                <p className="text-uppercase fs-6">{duration}</p>
                <p className="fs-4 mb-0">
                  <InlineMarkdown>{blurb}</InlineMarkdown>
                </p>
              </div>
            </div>
          </div>
        })
      }
    </div>
    {hasButtons && (
      <div className="d-grid gap-2 d-md-flex justify-content-center mt-4">
        {
          _.map(buttons, (button, i) => {
            return <ConfiguredButton key={i} config={button} className="btn-lg px-4 me-md-2" />
          })
        }
      </div>
    )}
  </div>
}

export default withValidatedSectionConfig(validateStepOverviewTemplateConfig, StepOverviewTemplate)
