import classNames from 'classnames'
import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClock } from '@fortawesome/free-regular-svg-icons'

import { ButtonConfig } from 'api/api'
import PearlImage, { PearlImageConfig } from 'util/PearlImage'
import { getSectionStyle } from 'util/styleUtils'

import ConfiguredButton from './ConfiguredButton'

type ParticipationDetailTemplateConfig = {
  blurb?: string, //  text below the title
  actionButton?: ButtonConfig, // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  image?: PearlImageConfig, // image
  stepNumberText?: string, // e.g. STEP 1
  timeIndication?: string, // e.g. 45+ minutes
  imagePosition?: string // left or right.  Default is right
}

type ParticipationDetailTemplateProps = {
  anchorRef?: string
  config: ParticipationDetailTemplateConfig
}

/**
 * Template for a participation step description
 */
function ParticipationDetailTemplate(props: ParticipationDetailTemplateProps) {
  const { anchorRef, config } = props
  const {
    blurb,
    actionButton,
    stepNumberText,
    timeIndication,
    image,
    imagePosition,
    title
  } = config

  const isLeftImage = imagePosition === 'left' // default is right, so left has to be explicitly specified
  return <div id={anchorRef} className="row mx-0 py-5" style={getSectionStyle(config)}>
    <div
      className={classNames(
        'col-md-10 col-lg-8', 'mx-auto', 'row',
        'justify-content-between',
        isLeftImage ? 'flex-row' : 'flex-row-reverse'
      )}
    >
      <div className="col-6 col-md-3 mx-auto mx-md-0 text-center">
        <PearlImage image={image} className="img-fluid mb-4 mb-md-0"/>
      </div>
      <div className="col-md-8">
        <h4>
          {stepNumberText}
        </h4>
        <p><FontAwesomeIcon icon={faClock}/> {timeIndication}</p>
        <h2>{title}</h2>
        <p className="fs-4">
          {blurb}
        </p>
        {actionButton && <ConfiguredButton config={actionButton} />}
      </div>
    </div>
  </div>
}

export default ParticipationDetailTemplate
