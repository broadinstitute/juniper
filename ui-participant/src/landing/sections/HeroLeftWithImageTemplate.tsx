import _ from 'lodash'
import React, { CSSProperties } from 'react'
import { ButtonConfig, getImageUrl } from 'api/api'
import PearlImage from '../../util/PearlImage'
import ConfiguredButton from './ConfiguredButton'

type Logo = {
  imageShortcode: string,
  alt: string
}

type HeroLeftWithImageTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  backgroundColor?: string, // background color for the block
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  imageShortcode?: string, // image
  backgroundImageShortcode?: string // background image
  logos?: Logo[]
}

/**
 * Template for a hero with text content on the left and an image on the right.
 */
function HeroLeftWithImageTemplate({
  config: { background, blurb, buttons, imageShortcode, backgroundImageShortcode, logos, title }
}: {config: HeroLeftWithImageTemplateProps}) {
  const styleProps: CSSProperties = { background }
  if (backgroundImageShortcode) {
    styleProps.backgroundImage = `url('${getImageUrl(backgroundImageShortcode)}')`
  }
  return <div className="row flex-lg-row-reverse"
    style={styleProps}>
    <div className="col-10 col-sm-8 col-lg-6 p-0">
      <PearlImage imageShortcode={imageShortcode} alt={''}
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
        {_.map(logos, ({ imageShortcode, alt }) => {
          return <PearlImage imageShortcode={imageShortcode} alt={alt} className={'m-1'}/>
        }) }
      </div>
    </div>
  </div>
}

export default HeroLeftWithImageTemplate
