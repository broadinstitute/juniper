import classNames from 'classnames'
import _ from 'lodash'
import React from 'react'

import { SectionConfig } from 'src/types/landingPageConfig'
import { getSectionStyle } from '../../util/styleUtils'
import { withValidatedSectionConfig } from '../../util/withValidatedSectionConfig'
import {
  requireOptionalArray,
  requireOptionalNumber,
  requireOptionalString
} from '../../util/validationUtils'

import './HeroWithImageTemplate.scss'

import ConfiguredButton, {
  ButtonConfig,
  buttonConfigProps,
  validateButtonConfig
} from '../ConfiguredButton'
import ConfiguredMedia, {
  ImageConfig,
  MediaConfig,
  mediaConfigProps,
  validateMediaConfig
} from '../ConfiguredMedia'
import {
  InlineMarkdown,
  Markdown
} from '../Markdown'

import { TemplateComponentProps } from './templateUtils'
import { useApiContext } from '../../ApiProvider'
import {
  blurbProp,
  SectionProp,
  titleProp
} from './SectionProp'

export type HeroWithImageTemplateConfig = {
  blurb?: string, //  text below the title
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  fullWidth?: boolean, // span the full page width or not
  image?: MediaConfig, // image
  imageAsBackground?: boolean, // use image as background image
  aspectRatio?: string, // aspect ratio of the image
  imagePosition?: 'left' | 'right', // left or right.  Default is right
  imageWidthPercentage?: number, // number between 0 and 100. Percentage of row width given to image.
  blurbSize?: string, // size of the blurb text
  logos?: MediaConfig[],
  title?: string // large heading text
}

export const heroWithImageTemplateConfigProps: SectionProp[] = [
  titleProp,
  blurbProp,
  { name: 'blurbSize' },
  { name: 'buttons', subProps: buttonConfigProps, isArray: true },
  { name: 'fullWidth' },
  { name: 'imageAsBackground' },
  { name: 'aspectRatio' },
  { name: 'image', subProps: mediaConfigProps },
  { name: 'imagePosition' },
  { name: 'imageWidthPercentage' },
  { name: 'logos', subProps: mediaConfigProps, isArray: true }
]


/** Validate that a section configuration object conforms to HeroWithImageTemplateConfig */
const validateHeroWithImageTemplateConfig = (config: SectionConfig): HeroWithImageTemplateConfig => {
  const message = 'Invalid HeroWithImageTemplateConfig'
  const blurb = requireOptionalString(config, 'blurb', message)
  const blurbSize = requireOptionalString(config, 'blurbSize', message)
  const buttons = requireOptionalArray(config, 'buttons', validateButtonConfig, message)
  const fullWidth = !!config.fullWidth
  const imageAsBackground = !!config.imageAsBackground
  const aspectRatio = requireOptionalString(config, 'aspectRatio', message)
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
    blurbSize,
    buttons,
    fullWidth,
    imageAsBackground,
    aspectRatio,
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
    blurbSize,
    buttons,
    fullWidth = false,
    imageAsBackground = false,
    aspectRatio,
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
          isLeftImage ? 'flex-row' : 'flex-row-reverse',
          imageAsBackground ? 'responsive-bg-image' : ''
        )}
        style={imageAsBackground ? {
          backgroundImage: `url(${
            getImageUrl((image as ImageConfig).cleanFileName, (image as ImageConfig).version)
          })`,
          backgroundSize: 'cover',
          backgroundRepeat: 'no-repeat',
          backgroundPosition: '50% 50%',
          width: '100%',
          aspectRatio: aspectRatio ? aspectRatio : undefined
        } : {}}
      >
        {!!image && (
          <div
            className={classNames(
              'col-12', `col-lg-${imageCols}`,
              'd-flex justify-content-center align-items-center p-0'
            )}
          >
            {!imageAsBackground && <ConfiguredMedia media={image} className="img-fluid"/>}
          </div>
        )}
        <div
          className={classNames(
            'col-12', `col-lg-${12 - imageCols}`,
            'py-3 p-sm-3 p-lg-5',
            'd-flex flex-column flex-grow-1 justify-content-center'
          )}
        >
          {!!title && (
            <h2 className={classNames('fs-1 fw-normal lh-sm', hasContentFollowingTitle ? 'mb-4' : 'mb-0')}>
              <InlineMarkdown>{title}</InlineMarkdown>
            </h2>
          )}
          {hasBlurb && (
            <Markdown className={classNames(blurbSize ? blurbSize : 'fs-4', { 'mb-4': hasContentFollowingBlurb })}>
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
