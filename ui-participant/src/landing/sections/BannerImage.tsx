import React from 'react'
import PearlImage, { PearlImageConfig } from '../../util/PearlImage'

type BannerImageConfig = {
  image?: PearlImageConfig
}

type BannerImageProps = {
  config: BannerImageConfig
}

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
          <PearlImage image={image} style={{ width: '100%', height: 'auto' }} />
        </div>
      )}
    </div>
  )
}

export default BannerImage
