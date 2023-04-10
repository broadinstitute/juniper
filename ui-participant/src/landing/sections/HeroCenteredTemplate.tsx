import _ from 'lodash'
import classNames from 'classnames'
import React from 'react'

import { SectionConfig } from 'api/api'
import { getSectionStyle } from 'util/styleUtils'
import { withValidatedSectionConfig } from 'util/withValidatedSectionConfig'
import { requireOptionalArray, requireOptionalString } from 'util/validationUtils'

import ConfiguredButton, { ButtonConfig, validateButtonConfig } from '../ConfiguredButton'
import { InlineMarkdown, Markdown } from '../Markdown'
import PearlImage, { PearlImageConfig, validatePearlImageConfig } from '../PearlImage'

import { TemplateComponentProps } from './templateUtils'

type HeroCenteredTemplateConfig = {
  blurb?: string, //  text below the title
  blurbAlign?: 'left' | 'right' | 'center' // left|right|center  where to align the blurb text.  default is 'center'
  buttons?: ButtonConfig[], // array of objects containing `text` and `href` attributes
  title?: string, // large heading text
  image?: PearlImageConfig   // image to display under blurb
}

/** Validate that a section configuration object conforms to HeroCenteredTemplateConfig */
const validateHeroCenteredTemplateConfig = (config: SectionConfig): HeroCenteredTemplateConfig => {
  const message = 'Invalid HeroCenteredTemplate config'
  const blurb = requireOptionalString(config, 'blurb', message)
  const blurbAlign = requireOptionalString(config, 'blurbAlign', message)
  if (!(blurbAlign === undefined || blurbAlign === 'left' || blurbAlign === 'right' || blurbAlign === 'center')) {
    throw new Error(`${message}: if provided, blurbAlign must be one of "left", "right", or "center"`)
  }

  const buttons = requireOptionalArray(config, 'buttons', validateButtonConfig)
  const title = requireOptionalString(config, 'title', message)
  const image = config.image ? validatePearlImageConfig(config.image) : undefined

  return {
    blurb,
    blurbAlign,
    buttons,
    title,
    image
  }
}

type HeroCenteredTemplateProps = TemplateComponentProps<HeroCenteredTemplateConfig>

/**
 * Template for rendering a hero with centered content.
 */
function HeroCenteredTemplate(props: HeroCenteredTemplateProps) {
  const { anchorRef, config } = props
  const { blurb, blurbAlign, buttons, title, image } = config

  const hasTitle = !!title
  const hasBlurb = !!blurb
  const hasImage = !!image
  const hasButtons = (buttons || []).length > 0

  const hasContentFollowingTitle = hasBlurb || hasImage || hasButtons
  const hasContentFollowingBlurb = hasImage || hasButtons
  const hasContentFollowingImage = hasButtons

  return <div id={anchorRef} className="row mx-0" style={getSectionStyle(config)}>
    <div className="col-12 col-sm-10 col-lg-6 mx-auto py-5 text-center">
      {hasTitle && (
        <h2 className={classNames('fs-1 fw-normal lh-sm', hasContentFollowingTitle ? 'mb-4' : 'mb-0')}>
          <InlineMarkdown>{title}</InlineMarkdown>
        </h2>
      )}
      {hasBlurb && (
        <Markdown
          className={classNames('fs-4', { 'mb-4': hasContentFollowingBlurb })}
          style={{ textAlign: blurbAlign || 'center' }}
        >
          {blurb}
        </Markdown>
      )}
      {hasImage && (
        <PearlImage image={image} className={classNames('img-fluid', { 'mb-4': hasContentFollowingImage })} />
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
