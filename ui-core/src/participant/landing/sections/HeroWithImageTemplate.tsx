import classNames from 'classnames'
import _ from 'lodash'
import React from 'react'

import { SectionConfig } from '../../../types/landingPageConfig'
import { getSectionStyle } from '../../util/styleUtils'
import { withValidatedSectionConfig } from '../../util/withValidatedSectionConfig'
import { requireOptionalArray, requireOptionalNumber, requireOptionalString }
  from '../../util/validationUtils'

import ConfiguredButton, { ButtonConfig, validateButtonConfig } from '../ConfiguredButton'
import ConfiguredMedia, { MediaConfig, validateMediaConfig } from '../ConfiguredMedia'
import { InlineMarkdown, Markdown } from '../Markdown'

import { TemplateComponentProps } from './templateUtils'
import { useApiContext } from '../../ApiProvider'

type HeroWithImageTemplateConfig = {
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  fullWidth?: boolean, // span the full page width or not
  image?: MediaConfig, // image
  imagePosition?: 'left' | 'right', // left or right.  Default is right
  imageWidthPercentage?: number, // number between 0 and 100. Percentage of row width given to image.
  logos?: MediaConfig[],
  title?: string // large heading text
}

/** Validate that a section configuration object conforms to HeroWithImageTemplateConfig */
const validateHeroWithImageTemplateConfig = (config: SectionConfig): HeroWithImageTemplateConfig => {
  const message = 'Invalid HeroWithImageTemplateConfig'
  const blurb = requireOptionalString(config, 'blurb', message)
  const buttons = requireOptionalArray(config, 'buttons', validateButtonConfig, message)
  const fullWidth = !!config.fullWidth
  const image = config.image ? validateMediaConfig(config.image) : undefined
  const imagePosition = requireOptionalString(config, 'imagePosition', message)
  if (!(imagePosition === undefined || imagePosition === 'left' || imagePosition === 'right')) {
    throw new Error(`${message}: if provided, imagePosition must be one of "left", "right"`)
  }
  const imageWidthPercentage = requireOptionalNumber(config, 'imageWidthPercentage', message)
  if (imageWidthPercentage !== undefined && (imageWidthPercentage < 0 || imageWidthPercentage > 100)) {
    throw new Error(`${message}: imageWidthPercentage must be between 0 and 100`)
  }
  const logos = requireOptionalArray(config, 'logos', validateMediaConfig, message)
  const title = requireOptionalString(config, 'title', message)
  return {
    blurb,
    buttons,
    fullWidth,
    image,
    imagePosition,
    imageWidthPercentage,
    logos,
    title
  }
}

type HeroWithImageTemplateProps = TemplateComponentProps<HeroWithImageTemplateConfig>

/**
 * Template for a hero with text content on the left and an image on the right.
 */
function HeroWithImageTemplate(props: HeroWithImageTemplateProps) {
  const { anchorRef, config } = props
  const {
    blurb,
    buttons,
    fullWidth = false,
    image,
    imagePosition,
    imageWidthPercentage: configuredImageWidthPercentage,
    logos,
    title
  } = config
  const { getImageUrl } = useApiContext()
  const isLeftImage = imagePosition === 'left' // default is right, so left has to be explicitly specified
  const imageWidthPercentage = _.isNumber(configuredImageWidthPercentage)
    ? _.clamp(configuredImageWidthPercentage, 0, 100)
    : (fullWidth ? 50 : 33)
  const imageCols = Math.max(Math.floor(imageWidthPercentage / 100 * 12), 1)

  const hasBlurb = !!blurb
  const hasButtons = (buttons || []).length > 0
  const hasLogos = (logos || []).length > 0

  const hasContentFollowingTitle = hasBlurb || hasButtons || hasLogos
  const hasContentFollowingBlurb = hasButtons || hasLogos
  const hasContentFollowingButtons = hasLogos

  return (
    <div
      className={classNames('row', 'py-0', 'mx-0', isLeftImage ? 'flex-row' : 'flex-row-reverse')}
      id={anchorRef}
      style={getSectionStyle(config, getImageUrl)}
    >
      <div
        className={classNames(
          'row',
          'col-12',
          fullWidth ? 'mx-0' : 'col-sm-10 mx-auto',
          isLeftImage ? 'flex-row' : 'flex-row-reverse'
        )}
      >
        {!!image && (
          <div
            className={classNames(
              'col-12', `col-lg-${imageCols}`,
              'd-flex justify-content-center align-items-center p-0'
            )}
          >
            <ConfiguredMedia media={image} className="img-fluid"/>
          </div>
        )}
        <div
          className={classNames(
            'col-12', `col-lg-${12 - imageCols}`,
            'py-3 p-sm-3 p-lg-5',
            'd-flex flex-column flex-grow-1 justify-content-around'
          )}
        >
          {!!title && (
            <h2 className={classNames('fs-1 fw-normal lh-sm', hasContentFollowingTitle ? 'mb-4' : 'mb-0')}>
              <InlineMarkdown>{title}</InlineMarkdown>
            </h2>
          )}
          {hasBlurb && (
            <Markdown className={classNames('fs-4', { 'mb-4': hasContentFollowingBlurb })}>
              {blurb}
            </Markdown>
          )}
          {hasButtons && (
            <div
              className={classNames(
                'd-grid gap-2 d-md-flex justify-content-md-start',
                { 'mb-4': hasContentFollowingButtons }
              )}
            >
              {
                _.map(buttons, (buttonConfig, i) =>
                  <ConfiguredButton key={i} config={buttonConfig} className="btn-lg px-4 me-md-2"/>
                )
              }
            </div>
          )}
          {hasLogos && (
            <div
              className={classNames(
                'd-flex',
                'flex-column align-items-center',
                'flex-sm-row align-items-sm-start flex-sm-wrap row-gap-4'
              )}
            >
              {_.map(logos, (logo, i) => {
                return (
                  <div style={{ width: '250px', paddingRight: '10px' }} key={i}>
                    <ConfiguredMedia
                      media={logo}
                      style={{ maxWidth: '100%' }}
                      className={classNames({ 'mt-4': i !== 0 }, 'mt-sm-0', 'me-sm-4')}
                    />
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default withValidatedSectionConfig(validateHeroWithImageTemplateConfig, HeroWithImageTemplate)
