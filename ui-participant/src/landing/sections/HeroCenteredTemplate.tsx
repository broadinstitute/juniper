import _ from 'lodash'
import React from 'react'
import { ButtonConfig } from 'api/api'
import ReactMarkdown from 'react-markdown'
import ConfiguredButton from './ConfiguredButton'
import PearlImage, { PearlImageConfig } from '../../util/PearlImage'

type HeroCenteredTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  backgroundColor?: string, // background color for the block
  blurb?: string, //  text below the title
  blurbAlign?: string // left|right|center  where to align the blurb text.  default is 'center'
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  color?: string, // foreground text color
  image?: PearlImageConfig   // image to display under blurb
}

const blurbAlignAllowed = ['center', 'right', 'left']

/**
 * Template for rendering a hero with centered content.
 */
function HeroCenteredTemplate({
  config: {
    background, backgroundColor, color,
    blurb, blurbAlign, buttons, title, image
  }
}:
                                { config: HeroCenteredTemplateProps }) {
  const blurbAlignIndex = blurbAlignAllowed.indexOf(blurbAlign ?? 'center')
  const cleanBlurbAlign: string = blurbAlignAllowed[blurbAlignIndex === -1 ? 0 : blurbAlignIndex] ?? 'center'
  const blurbStyle = {
    textAlign: cleanBlurbAlign as CanvasTextAlign
  }
  return <div className="row mx-0" style={{ background, backgroundColor, color }}>
    <div className="col-12 col-sm-10 col-lg-6 mx-auto py-5 text-center">
      {!!title && (
        <h1 className="fs-1 fw-normal lh-sm mb-4">
          <ReactMarkdown>{title}</ReactMarkdown>
        </h1>
      )}
      {!!blurb && (
        <div className="fs-5 " style={blurbStyle}>
          <ReactMarkdown>{blurb}</ReactMarkdown>
        </div>
      )}
      {!!image && (
        <PearlImage image={image} className="img-fluid mb-4"/>
      )}
      {(buttons ?? []).length > 0 && (
        <div className="d-grid gap-2 d-sm-flex justify-content-sm-center">
          {
            _.map(buttons, (button, i) => {
              // TODO: allow customization of button styling
              return <ConfiguredButton key={i} config={button} className='btn btn-light btn-lg px-4 me-md-2'/>
            })
          }
        </div>
      )}
    </div>
  </div>
}

export default HeroCenteredTemplate
