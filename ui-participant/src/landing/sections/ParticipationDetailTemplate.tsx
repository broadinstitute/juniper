import classNames from 'classnames'
import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClock } from '@fortawesome/free-regular-svg-icons'

import { SectionConfig } from 'api/api'
import { getSectionStyle } from 'util/styleUtils'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalString } from 'util/validationUtils'

import ConfiguredButton, { ButtonConfig, validateButtonConfig } from '../ConfiguredButton'
import PearlImage, { PearlImageConfig, validatePearlImageConfig } from '../PearlImage'
import { SectionHeading } from '../SectionHeading'

import { TemplateComponentProps } from './templateUtils'

type ParticipationDetailTemplateConfig = {
  actionButton?: ButtonConfig, // button
  blurb?: string, //  text below the title
  image?: PearlImageConfig, // image
  imagePosition?: 'left' | 'right' // left or right.  Default is right
  stepNumberText?: string, // e.g. STEP 1
  timeIndication?: string, // e.g. 45+ minutes
  title?: string, // large heading text
}

/** Validate that a section configuration object conforms to ParticipationDetailTemplateConfig */
const validateParticipationDetailTemplateConfig = (config: SectionConfig): ParticipationDetailTemplateConfig => {
  const message = 'Invalid ParticipationDetailTemplateConfig'
  const actionButton = config.actionButton ? validateButtonConfig(config.actionButton) : undefined
  const blurb = requireOptionalString(config, 'blurb', message)
  const image = config.image ? validatePearlImageConfig(config.image) : undefined
  const imagePosition = requireOptionalString(config, 'imagePosition', message)
  if (!(imagePosition === undefined || imagePosition === 'left' || imagePosition === 'right')) {
    throw new Error(`${message}: if provided, imagePosition must be one of "left", "right"`)
  }
  const stepNumberText = requireOptionalString(config, 'stepNumberText', message)
  const timeIndication = requireOptionalString(config, 'timeIndication', message)
  const title = requireOptionalString(config, 'title', message)

  return {
    actionButton,
    blurb,
    image,
    imagePosition,
    stepNumberText,
    timeIndication,
    title
  }
}

type ParticipationDetailTemplateProps = TemplateComponentProps<ParticipationDetailTemplateConfig>

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

  const hasImage = !!image
  const isLeftImage = imagePosition === 'left' // default is right, so left has to be explicitly specified
  return <div id={anchorRef} className="row mx-0 py-5" style={getSectionStyle(config)}>
    <div
      className={classNames(
        'col-md-10 col-lg-8', 'mx-auto', 'row',
        { 'justify-content-between': hasImage },
        { 'flex-row': hasImage && isLeftImage },
        { 'flex-row-reverse': hasImage && !isLeftImage }
      )}
    >
      {hasImage && (
        <div className="col-6 col-md-3 mx-auto mx-md-0 text-center">
          <PearlImage image={image} className="img-fluid mb-4 mb-md-0"/>
        </div>
      )}
      <div className={classNames({ 'col-md-8': hasImage })}>
        <SectionHeading className="h2">
          <div className="h4">{stepNumberText}</div>
          {title}
        </SectionHeading>
        <p><FontAwesomeIcon icon={faClock}/> {timeIndication}</p>
        <p className="fs-4">
          {blurb}
        </p>
        {actionButton && <ConfiguredButton config={actionButton} />}
      </div>
    </div>
  </div>
}

export default withValidatedSectionConfig(validateParticipationDetailTemplateConfig, ParticipationDetailTemplate)
