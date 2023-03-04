import React, { CSSProperties } from 'react'
import { ButtonConfig } from 'api/api'
import PearlImage, { PearlImageProps } from '../../util/PearlImage'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClock } from '@fortawesome/free-regular-svg-icons'
import ConfiguredButton from './ConfiguredButton'

type ParticipationDetailTemplateProps = {
  background?: string, // background CSS style (e.g. `linear-gradient(...)`)
  blurb?: string, //  text below the title
  actionButton?: ButtonConfig, // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  image?: PearlImageProps, // image
  stepNumberText?: string, // e.g. STEP 1
  timeIndication?: string, // e.g. 45+ minutes
  imagePosition?: string // left or right.  Default is right
}

/**
 * Template for a participation step description
 */
function ParticipationDetailTemplate({
  anchorRef,
  config: {
    background,
    blurb,
    actionButton,
    stepNumberText,
    timeIndication,
    image,
    imagePosition,
    title
  }
}: { anchorRef: string, config: ParticipationDetailTemplateProps }) {
  const styleProps: CSSProperties = { background }
  const isLeftImage = imagePosition === 'left' // default is right, so left has to be explicitly specified
  return <div id={anchorRef} className="row justify-content-center" style={styleProps}>
    <div className={`col-md-8 d-flex py-5 ${isLeftImage ? 'flex-row' : 'flex-row-reverse'}`}>
      <div className={`${isLeftImage ? 'pe-5' : 'ps-5'}`}>
        <PearlImage image={image} className="img-fluid"/>
      </div>
      <div className="flex-grow-1">
        <h4>
          {stepNumberText}
        </h4>
        <p><FontAwesomeIcon icon={faClock}/> {timeIndication}</p>
        <h2>{title}</h2>
        <p className="fs-4">
          {blurb}
        </p>
        {actionButton && <ConfiguredButton config={actionButton} className="btn btn-secondary"/>}
      </div>
    </div>
  </div>
}

export default ParticipationDetailTemplate
