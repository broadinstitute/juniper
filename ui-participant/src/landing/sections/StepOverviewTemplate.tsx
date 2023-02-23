import _ from 'lodash'
import React from 'react'
import { ButtonConfig } from 'api/api'
import PearlImage, { PearlImageProps } from 'util/PearlImage'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import ReactMarkdown from 'react-markdown'

type StepProps = {
  image: PearlImageProps,
  duration: string,
  blurb: string
}

type StepOverviewTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  steps?: StepProps[]
}

/**
 * Template for rendering a step overview
 */
function StepOverviewTemplate({
  config: {
    background,
    buttons,
    steps,
    title
  }
}: { config: StepOverviewTemplateProps }) {
  // TODO: improve layout code for better flexing, especially with <> 4 steps
  return <div style={{ background }} className="py-5">
    <h1 className="fs-1 fw-normal lh-sm mb-3 text-center">
      <ReactMarkdown>{title ? title : ''}</ReactMarkdown>
    </h1>
    <div className="row justify-content-center">
      <div className="col-md-10 d-flex">
        {
          _.map(steps, ({ image, duration, blurb }: StepProps, i: number) => {
            return <>
              {i > 0 ? <FontAwesomeIcon className="fa-2x p-3 mt-5" icon={faArrowRight}/> : null}
              <div className="d-flex flex-column">

                <PearlImage image={image} className="img-fluid p-3 mx-auto" style={{ maxWidth: '200px' }}/>
                <div className="text-uppercase">
                  <p className="fs-5 fw-semibold mb-0">Step {i + 1}</p>
                  <p className="fs-6">{duration}</p>
                </div>
                <div>
                  <p className="fs-5 fw-normal">
                    {blurb}
                  </p>
                </div>
              </div>
            </>
          })
        }
      </div>
    </div>
    <div className="d-grid gap-2 d-md-flex pt-4 justify-content-center">
      {
        _.map(buttons, ({ text, href }) => {
          return <a href={href} role={'button'} className="btn btn-primary btn-lg px-4 me-md-2">{text}</a>
        })
      }
    </div>
  </div>
}

export default StepOverviewTemplate
