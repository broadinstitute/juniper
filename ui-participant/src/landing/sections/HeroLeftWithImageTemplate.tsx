import _ from 'lodash'
import React, { CSSProperties } from 'react'
import { ButtonConfig, getImageUrl } from 'api/api'
import PearlImage, { PearlImageProps } from '../../util/PearlImage'
import ConfiguredButton from './ConfiguredButton'

type HeroLeftWithImageTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  backgroundColor?: string, // background color for the block
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  image?: PearlImageProps, // image
  backgroundImage?: PearlImageProps // background image
  logos?: PearlImageProps[]
}

/**
 * Template for a hero with text content on the left and an image on the right.
 */
function HeroLeftWithImageTemplate({
  config: {
    background,
    blurb,
    buttons,
    image,
    backgroundImage,
    logos,
    title
  }
}: { config: HeroLeftWithImageTemplateProps }) {
  const styleProps: CSSProperties = { background }
  if (backgroundImage) {
    styleProps.backgroundImage = `url('${getImageUrl(backgroundImage.cleanFileName, backgroundImage.version)}')`
  }
  return <div className="row flex-lg-row-reverse"
    style={styleProps}>
    <div className="col-10 col-sm-8 col-lg-6 p-0">
      <PearlImage image={image}
        className={'d-block mx-lg-auto img-fluid p-0'}/>
    </div>
    <div className="col-lg-6 ps-5 py-5 d-flex flex-column justify-content-around">
      <h1 className="fs-1 fw-normal lh-sm">
        {title}
      </h1>
      <p className="fs-5">
        {blurb}
      </p>
      <div className="d-grid gap-2 d-md-flex justify-content-md-start">
        {
          _.map(buttons, (buttonConfig, i) =>
            <ConfiguredButton key={i} config={buttonConfig} className="btn btn-primary btn-lg px-4 me-md-2"/>
          )
        }
      </div>
      <div className="d-flex flex-wrap align-items-center justify-content-between">
        {_.map(logos, logo => {
          return <PearlImage key={logo.cleanFileName} image={logo} className={'m-1'}/>
        })}
      </div>
    </div>
  </div>
}

export default HeroLeftWithImageTemplate
