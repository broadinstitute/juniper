import classNames from 'classnames'
import _ from 'lodash'
import React, { CSSProperties } from 'react'
import { ButtonConfig, getImageUrl } from 'api/api'
import PearlImage, { PearlImageConfig } from '../../util/PearlImage'
import ConfiguredButton from './ConfiguredButton'
import ReactMarkdown from 'react-markdown'

type HeroLeftWithImageTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  backgroundColor?: string, // background color for the block
  backgroundImage?: PearlImageConfig, // background image
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  image?: PearlImageConfig, // image
  imagePosition?: string, // left or right.  Default is right
  logos?: PearlImageConfig[]
}

/**
 * Template for a hero with text content on the left and an image on the right.
 */
function HeroWithImageTemplate({
  anchorRef,
  config: {
    background,
    blurb,
    buttons,
    image,
    imagePosition,
    backgroundImage,
    logos,
    title
  }
}: { anchorRef?: string, config: HeroLeftWithImageTemplateProps }) {
  const styleProps: CSSProperties = { background }
  if (backgroundImage) {
    styleProps.backgroundImage = `url('${getImageUrl(backgroundImage.cleanFileName, backgroundImage.version)}')`
  }
  const isLeftImage = imagePosition === 'left' // default is right, so left has to be explicitly specified
  return (
    <div id={anchorRef} className={classNames('row', 'mx-0', isLeftImage ? 'flex-row' : 'flex-row-reverse')}
      style={styleProps}>
      {!!image && (
        <div className="col-12 col-lg-6 d-flex justify-content-center align-items-center p-0">
          <PearlImage image={image} className="img-fluid"/>
        </div>
      )}
      <div
        className="col-12 col-lg-6 py-3 p-sm-3 p-lg-5 d-flex flex-column flex-grow-1 justify-content-around"
        style={{ minWidth: '50%' }}
      >
        {!!title && (
          <h1 className="fs-1 fw-normal lh-sm">
            <ReactMarkdown>{title}</ReactMarkdown>
          </h1>
        )}
        {!!blurb && (
          <div className="fs-5">
            <ReactMarkdown>{blurb}</ReactMarkdown>
          </div>
        )}
        {(buttons || []).length > 0 && (
          <div className="d-grid gap-2 d-md-flex justify-content-md-start">
            {
              _.map(buttons, (buttonConfig, i) =>
                <ConfiguredButton key={i} config={buttonConfig} className="btn-lg px-4 me-md-2"/>
              )
            }
          </div>
        )}
        {(logos || []).length > 0 && (
          <div className="d-flex flex-wrap align-items-center justify-content-between">
            {_.map(logos, logo => {
              return <PearlImage key={logo.cleanFileName} image={logo} className={'m-1'}/>
            })}
          </div>
        )}
      </div>
    </div>
  )
}

export default HeroWithImageTemplate
