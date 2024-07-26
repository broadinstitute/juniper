import _ from 'lodash'
import classNames from 'classnames'
import React from 'react'

import { SectionConfig } from '../../../types/landingPageConfig'
import { getSectionStyle } from '../../util/styleUtils'
import { withValidatedSectionConfig } from '../../util/withValidatedSectionConfig'
import {
  requireOptionalArray,
  requireOptionalString
} from '../../util/validationUtils'

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
import {
  InlineMarkdown,
  Markdown
} from '../Markdown'

import { TemplateComponentProps } from './templateUtils'
import { useApiContext } from '../../../participant/ApiProvider'

export type HeroCenteredTemplateConfig = {
  blurb?: string, //  text below the title
  blurbAlign?: 'left' | 'right' | 'center' // left|right|center  where to align the blurb text.  default is 'center'
  blurbSize?: string
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  image?: MediaConfig   // image to display under blurb
}

/** Validate that a section configuration object conforms to HeroCenteredTemplateConfig */
const validateHeroCenteredTemplateConfig = (config: SectionConfig): HeroCenteredTemplateConfig => {
  const message = 'Invalid HeroCenteredTemplate config'
  const blurb = requireOptionalString(config, 'blurb', message)
  const blurbAlign = requireOptionalString(config, 'blurbAlign', message)
  if (!(blurbAlign === undefined || blurbAlign === 'left' || blurbAlign === 'right' || blurbAlign === 'center')) {
    throw new Error(`${message}: if provided, blurbAlign must be one of "left", "right", or "center"`)
  }

  const blurbSize = requireOptionalString(config, 'blurbSize', message)
  const buttons = requireOptionalArray(config, 'buttons', validateButtonConfig)
  const title = requireOptionalString(config, 'title', message)
  const image = config.image ? validateMediaConfig(config.image) : undefined

  return {
    blurb,
    blurbAlign,
    blurbSize,
    buttons,
    title,
    image
  }
}

export const heroCenteredTemplateConfigProps = [
  { name: 'blurb', translated: true },
  { name: 'blurbAlign' },
  { name: 'blurbSize' },
  { name: 'buttons', subProps: buttonConfigProps, isArray: true },
  { name: 'title', translated: true },
  { name: 'image', subProps: mediaConfigProps }
]

type HeroCenteredTemplateProps = TemplateComponentProps<HeroCenteredTemplateConfig>

/**
 * Template for rendering a hero with centered content.
 */
function HeroCenteredTemplate(props: HeroCenteredTemplateProps) {
  const { anchorRef, config } = props
  const { blurb, blurbAlign, buttons, title, image, blurbSize } = config
  const { getImageUrl } = useApiContext()
  const hasTitle = !!title
  const hasBlurb = !!blurb
  const hasImage = !!image
  const hasButtons = (buttons || []).length > 0

  const hasContentFollowingTitle = hasBlurb || hasImage || hasButtons
  const hasContentFollowingBlurb = hasImage || hasButtons
  const hasContentFollowingImage = hasButtons

  return <div id={anchorRef} className="row mx-0" style={getSectionStyle(config, getImageUrl)}>
    <div className="col-12 col-sm-10 col-lg-6 mx-auto text-center">
      {hasTitle && (
        <h2 className={classNames('fs-1 fw-normal lh-sm', hasContentFollowingTitle ? 'mb-4' : 'mb-0')}>
          <InlineMarkdown>{title}</InlineMarkdown>
        </h2>
      )}
      {hasBlurb && (
        <Markdown
          className={classNames(blurbSize ? blurbSize : 'fs-4', { 'mb-4': hasContentFollowingBlurb })}
          style={{ textAlign: blurbAlign || 'center' }}
        >
          {blurb}
        </Markdown>
      )}
      {hasImage && (
        <ConfiguredMedia media={image} className={classNames('img-fluid', { 'mb-4': hasContentFollowingImage })} />
      )}
      {hasButtons && (
        <div className="d-grid gap-2 d-sm-flex justify-content-sm-center">
          {
            _.map(buttons, (button, i) => {
              // TODO: allow customization of button styling
              return <ConfiguredButton key={i} config={button} className="btn-lg px-4 mx-md-1" />
            })
          }
        </div>
      )}
    </div>
  </div>
}

export default withValidatedSectionConfig(validateHeroCenteredTemplateConfig, HeroCenteredTemplate)
