import _ from 'lodash'
import React from 'react'

type StepProps = {
  imageStableId: string,
  duration: string,
  blurb: string
}

type StepOverviewTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  buttons?: any, // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  steps?: StepProps[]
}

/**
 * Template for rendering a step overview
 */
function StepOverviewTemplate({config: {
      background,
      buttons,
      steps,
      title
  }}: {config: StepOverviewTemplateProps}) {

  // TODO: improve layout code for better flexing, especially with <> 4 steps
  return <div className="p-5" style={{background}}>
    <h1 className="fs-1 fw-normal lh-sm mb-3 text-center">
      {title}
    </h1>
    <div className={'row'}>
      {
        _.map(steps, ({ imageStableId, duration, blurb }: StepProps, i: number) => {
          return <>
            { i > 0 ? <div className={'col'}>→</div> : null }
            <div className={'col-2'}>
              <div className="d-flex flex-column">
                <div>
                  {/**<ArborImage imageStableId={imageStableId} alt={''} className={'img-fluid'}/>*/}
                </div>
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
            </div>
          </>
        })
      }
    </div>
    <div className="d-grid gap-2 d-md-flex justify-content-md-start">
      {
        _.map(buttons, ({ text, href }) => {
          return <a href={href} role={'button'} className="btn btn-primary btn-lg px-4 me-md-2">{text}</a>
        })
      }
    </div>
  </div>
}

export default StepOverviewTemplate
