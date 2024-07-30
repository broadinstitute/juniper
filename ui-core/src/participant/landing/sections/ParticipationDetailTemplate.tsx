import classNames from 'classnames'
import React from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClock } from '@fortawesome/free-regular-svg-icons'

import { SectionConfig } from '../../../types/landingPageConfig'
import { getSectionStyle } from '../../util/styleUtils'
import { withValidatedSectionConfig } from '../../util/withValidatedSectionConfig'
import { requireOptionalString } from '../../util/validationUtils'

import ConfiguredButton, {
  ButtonConfig,
  buttonConfigProps,
  validateButtonConfig
} from '../ConfiguredButton'
import ConfiguredMedia, {
  MediaConfig,
  mediaConfigProps,
  validateMediaConfig
} from '../ConfiguredMedia'

import { TemplateComponentProps } from './templateUtils'
import { useApiContext } from '../../../participant/ApiProvider'
import {
  blurbProp,
  titleProp
} from './SectionProp'
import { InlineMarkdown } from '../../../participant/landing/Markdown'

type ParticipationDetailTemplateConfig = {
  actionButton?: ButtonConfig, // button
  blurb?: string, //  text below the title
  blurbSize?: string, // size of the blurb text
  image?: MediaConfig, // image
  imagePosition?: 'left' | 'right' // left or right.  Default is right
  stepNumberText?: string, // e.g. STEP 1
  timeIndication?: string, // e.g. 45+ minutes
  title?: string, // large heading text
}

export const participationDetailTemplateConfigProps = [
  titleProp,
  blurbProp,
  { name: 'blurbSize' },
  { name: 'actionButton', subProps: buttonConfigProps },
  { name: 'image', subProps: mediaConfigProps },
  { name: 'imagePosition' },
  { name: 'stepNumberText', translated: true },
  { name: 'timeIndication', translated: true }
]

/** Validate that a section configuration object conforms to ParticipationDetailTemplateConfig */
const validateParticipationDetailTemplateConfig = (config: SectionConfig): ParticipationDetailTemplateConfig => {
  const message = 'Invalid ParticipationDetailTemplateConfig'
  const actionButton = config.actionButton ? validateButtonConfig(config.actionButton) : undefined
  const blurb = requireOptionalString(config, 'blurb', message)
  const image = config.image ? validateMediaConfig(config.image) : undefined
  const imagePosition = requireOptionalString(config, 'imagePosition', message)
  if (!(imagePosition === undefined || imagePosition === 'left' || imagePosition === 'right')) {
    throw new Error(`${message}: if provided, imagePosition must be one of "left", "right"`)
  }
  const stepNumberText = requireOptionalString(config, 'stepNumberText', message)
  const timeIndication = requireOptionalString(config, 'timeIndication', message)
  const title = requireOptionalString(config, 'title', message)
  const blurbSize = requireOptionalString(config, 'blurbSize', message)

  return {
    actionButton,
    blurb,
    blurbSize,
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
    blurbSize,
    actionButton,
    stepNumberText,
    timeIndication,
    image,
    imagePosition,
    title
  } = config
  const { getImageUrl } = useApiContext()

  const hasImage = !!image
  const isLeftImage = imagePosition === 'left' // default is right, so left has to be explicitly specified
  return <div id={anchorRef} className="row mx-0" style={getSectionStyle(config, getImageUrl)}>
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
          <ConfiguredMedia media={image} className="img-fluid mb-4 mb-md-0"/>
        </div>
      )}
      <div className={classNames({ 'col-md-8': hasImage })}>
        <h2>
          <div className="h4">{stepNumberText}</div>
          {title && <InlineMarkdown>{title}</InlineMarkdown>}
        </h2>
        {timeIndication && <p><FontAwesomeIcon icon={faClock}/> {timeIndication}</p>}
        {blurb && <p className={classNames(blurbSize ? blurbSize : 'fs-4', actionButton ? 'mb-4' : 'mb-0')}>
          <InlineMarkdown>{blurb}</InlineMarkdown>
        </p> }
        {actionButton && <ConfiguredButton config={actionButton} className="btn-lg"/>}
      </div>
    </div>
  </div>
}

export default withValidatedSectionConfig(validateParticipationDetailTemplateConfig, ParticipationDetailTemplate)
