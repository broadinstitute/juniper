import React from 'react'

import { SectionConfig } from '../../../types/landingPageConfig'
import { withValidatedSectionConfig } from '../../util/withValidatedSectionConfig'

import ConfiguredImage, { ImageConfig, validateImageConfig } from '../ConfiguredImage'

import { TemplateComponentProps } from './templateUtils'

type BannerImageConfig = {
  image: ImageConfig
}

const validateBannerImageConfig = (config: SectionConfig): BannerImageConfig => {
  const image = validateImageConfig(config.image)
  return { image }
}

type BannerImageProps = TemplateComponentProps<BannerImageConfig>

/**
 * Template for a full width banner image.
 */
function BannerImage(props: BannerImageProps) {
  const {
    config: {
      image
    }
  } = props

  return (
    <div className="row mx-0">
      {!!image && (
        <div className="col-12 p-0">
          <ConfiguredImage image={image} style={{ width: '100%', height: 'auto' }} />
        </div>
      )}
    </div>
  )
}

export default withValidatedSectionConfig(validateBannerImageConfig, BannerImage)
