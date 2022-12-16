import React from 'react'
import PearlImage from '../../util/PearlImage'

type HeroRightWithImageTemplateProps = {
  blurb?: string, //  text below the title
  title?: string, // large heading text
  imageShortcode?: string, // image
}

/**
 * Template for a hero with text content on the right and an image on the left.
 *  TODO - implement images
 * @param {TemplateProps} props
 * @param {string?} props.content.title - large heading text
 * @param {string?} props.content.blurb - text below the title
 * @param {string?} props.content.imageStableId - stable ID of the image
 */
function HeroRightWithImageTemplate({
  config: {
    blurb,
    imageShortcode,
    title
  }
}: {config: HeroRightWithImageTemplateProps}) {
  return <div className="row">
    <div className="col-lg-6">
      <PearlImage imageShortcode={imageShortcode} alt={''} className="img-fluid"/>
    </div>
    <div className="col-lg-6 px-5">
      <div className="d-flex flex-column justify-content-center h-100">
        <h1 className="fs-1 fw-normal lh-sm mb-4">
          {title}
        </h1>
        <p className="fs-5">
          {blurb}
        </p>
      </div>
    </div>
  </div>
}

export default HeroRightWithImageTemplate
