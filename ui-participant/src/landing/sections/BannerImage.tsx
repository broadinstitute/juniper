import classNames from 'classnames'
import _ from 'lodash'
import React, { CSSProperties } from 'react'
import { ButtonConfig, getImageUrl } from 'api/api'
import PearlImage, { PearlImageConfig } from '../../util/PearlImage'
import ConfiguredButton from './ConfiguredButton'
import ReactMarkdown from 'react-markdown'

type BannerImageConfig = {
  image?: PearlImageConfig
}

type BannerImageProps = {
  config: BannerImageConfig
}

/**
 * Template for a hero with text content on the left and an image on the right.
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
          <PearlImage image={image} style={{ width: '100%', height: 'auto' }} />
        </div>
      )}
    </div>
  )
}

export default BannerImage
