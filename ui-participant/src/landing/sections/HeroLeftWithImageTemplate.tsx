import _ from 'lodash'
import React from 'react'
import { ButtonConfig } from 'api/api'
import PearlImage from '../../util/PearlImage'

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
  logos?: Logo[]
}

/**
 * Template for a hero with text content on the left and an image on the right.
 * TODO -- implement images
 */
function HeroLeftWithImageTemplate({
  config: { background, blurb, buttons, imageShortcode, logos, title }
}: {config: HeroLeftWithImageTemplateProps}) {
  return <div className="row flex-lg-row-reverse"
    style={{ background }}>
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
          _.map(buttons, ({ text, href }) => {
            return <a key={href} href={href} role={'button'} className="btn btn-primary btn-lg px-4 me-md-2">{text}</a>
          })
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
