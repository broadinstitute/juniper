import _ from 'lodash'
import classNames from 'classnames'
import React from 'react'
import ReactMarkdown from 'react-markdown'

import { ButtonConfig } from 'api/api'
import PearlImage, { PearlImageConfig } from 'util/PearlImage'
import { getSectionStyle } from 'util/styleUtils'

import ConfiguredButton from './ConfiguredButton'

type HeroCenteredTemplateConfig = {
  blurb?: string, //  text below the title
  blurbAlign?: string // left|right|center  where to align the blurb text.  default is 'center'
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  image?: PearlImageConfig   // image to display under blurb
}

type HeroCenteredTemplateProps = {
  anchorRef?: string
  config: HeroCenteredTemplateConfig
}

const blurbAlignAllowed = ['center', 'right', 'left']

/**
 * Template for rendering a hero with centered content.
 */
function HeroCenteredTemplate(props: HeroCenteredTemplateProps) {
  const { anchorRef, config } = props
  const { blurb, blurbAlign, buttons, title, image } = config

  const blurbAlignIndex = blurbAlignAllowed.indexOf(blurbAlign ?? 'center')
  const cleanBlurbAlign: string = blurbAlignAllowed[blurbAlignIndex === -1 ? 0 : blurbAlignIndex] ?? 'center'
  const blurbStyle = {
    textAlign: cleanBlurbAlign as CanvasTextAlign
  }

  const hasTitle = !!title
  const hasBlurb = !!blurb
  const hasImage = !!image
  const hasButtons = (buttons || []).length > 0

  const hasContentFollowingTitle = hasBlurb || hasImage || hasButtons
  const hasContentFollowingImage = hasButtons

  return <div id={anchorRef} className="row mx-0" style={getSectionStyle(config)}>
    <div className="col-12 col-sm-10 col-lg-6 mx-auto py-5 text-center">
      {hasTitle && (
        <h1 className={classNames('fs-1 fw-normal lh-sm', hasContentFollowingTitle ? 'mb-4' : 'mb-0')}>
          <ReactMarkdown disallowedElements={['p']} unwrapDisallowed>{title}</ReactMarkdown>
        </h1>
      )}
      {hasBlurb && (
        <div className="fs-4" style={blurbStyle}>
          <ReactMarkdown>{blurb}</ReactMarkdown>
        </div>
      )}
      {hasImage && (
        <PearlImage image={image} className={classNames('img-fluid', { 'mb-4': hasContentFollowingImage })} />
      )}
      {hasButtons && (
        <div className="d-grid gap-2 d-sm-flex justify-content-sm-center">
          {
            _.map(buttons, (button, i) => {
              // TODO: allow customization of button styling
              return <ConfiguredButton key={i} config={button} className='btn-lg px-4 me-md-2'/>
            })
          }
        </div>
      )}
    </div>
  </div>
}

export default HeroCenteredTemplate
