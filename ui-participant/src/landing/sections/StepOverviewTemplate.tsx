import _ from 'lodash'
import React from 'react'
import ReactMarkdown from 'react-markdown'

import { ButtonConfig } from 'api/api'
import PearlImage, { PearlImageConfig } from 'util/PearlImage'
import { getSectionStyle } from 'util/styleUtils'

type StepConfig = {
  image: PearlImageConfig,
  duration: string,
  blurb: string
}

type StepOverviewTemplateConfig = {
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  steps?: StepConfig[]
}

type StepOverviewTemplateProps = {
  anchorRef?: string
  config: StepOverviewTemplateConfig
}

/**
 * Template for rendering a step overview
 */
function StepOverviewTemplate(props: StepOverviewTemplateProps) {
  const { anchorRef, config } = props
  const { buttons, steps, title } = config

  // TODO: improve layout code for better flexing, especially with <> 4 steps
  return <div id={anchorRef} className="py-5" style={getSectionStyle(config)}>
    <h1 className="fs-1 fw-normal lh-sm mb-3 text-center">
      <ReactMarkdown>{title ? title : ''}</ReactMarkdown>
    </h1>
    <div className="row mx-0">
      {
        _.map(steps, ({ image, duration, blurb }: StepConfig, i: number) => {
          return <div key={i} className="col-12 col-lg-3 d-flex flex-column align-items-center">
            <div className="w-75 d-flex flex-column align-items-center align-items-lg-start">
              <PearlImage image={image} className="img-fluid p-3" style={{ maxWidth: '200px' }}/>
              <p className="text-uppercase fs-5 fw-semibold mb-0">Step {i + 1}</p>
              <p className="text-uppercase fs-6">{duration}</p>
              <p className="fs-4">
                {blurb}
              </p>
            </div>
          </div>
        })
      }
    </div>
    <div className="d-grid gap-2 d-md-flex pt-4 justify-content-center">
      {
        _.map(buttons, ({ text, href }, i) => {
          return <a key={i} href={href} className="btn btn-outline-primary btn-lg px-4 me-md-2">{text}</a>
        })
      }
    </div>
  </div>
}

export default StepOverviewTemplate
