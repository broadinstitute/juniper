import _ from 'lodash'
import React from 'react'
import {ButtonConfig} from 'api/api'
import ReactMarkdown from 'react-markdown'

type HeroCenteredTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  backgroundColor?: string, // background color for the block
  blurb?: string, //  text below the title
  blurbAlign?: string // left|right|center  where to align the blurb text.  default is 'center'
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  color?: string // foreground text color
}

const blurbAlignAllowed = ['center', 'right', 'left']

/**
 * Template for rendering a hero with centered content.
 */
function HeroCenteredTemplate({config: {background, backgroundColor, color, blurb, blurbAlign, buttons, title}}:
                                { config: HeroCenteredTemplateProps }) {
  const blurbAlignIndex = blurbAlignAllowed.indexOf(blurbAlign ?? 'center')
  const cleanBlurbAlign: string = blurbAlignAllowed[blurbAlignIndex === -1 ? 0 : blurbAlignIndex] ?? 'center'
  const blurbStyle = {
    textAlign: cleanBlurbAlign as CanvasTextAlign
  }
  return <div className="py-5 text-center" style={{background, backgroundColor, color}}>
    <div className="col-lg-6 mx-auto">
      <h1 className="fs-1 fw-normal lh-sm mb-4">
        {title}
      </h1>
      <p className="fs-5 " style={blurbStyle}>
        <ReactMarkdown>{blurb ? blurb : ''}</ReactMarkdown>
      </p>
    </div>
    <div className="d-grid gap-2 d-sm-flex justify-content-sm-center">
      {
        _.map(buttons, ({text, href}) => {
          // TODO: allow customization of button styling
          return <a href={href} role={'button'} className="btn btn-light btn-lg px-4 me-md-2">{text}</a>
        })
      }
    </div>
  </div>
}

export default HeroCenteredTemplate
