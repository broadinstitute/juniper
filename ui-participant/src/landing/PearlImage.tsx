import classNames from 'classnames'
import React, { CSSProperties } from 'react'
import { getImageUrl } from 'api/api'

import { requireOptionalString, requireNumber, requirePlainObject, requireString } from 'util/validationUtils'

export type PearlImageConfig = {
  cleanFileName: string,
  version: number,
  alt?: string,
  className?: string
  style?: CSSProperties
}

export const validatePearlImageConfig = (imageConfig: unknown): PearlImageConfig => {
  const message = 'Invalid image config'
  const config = requirePlainObject(imageConfig, message)

  const cleanFileName = requireString(config, 'cleanFileName', message)
  const version = requireNumber(config, 'version', message)
  const alt = requireOptionalString(config, 'alt', message)
  const className = requireOptionalString(config, 'className', message)

  // Only validate that style is an object. React will handle invalid keys.
  const style = config.style ? requirePlainObject(config, 'style') : undefined

  return {
    cleanFileName,
    version,
    alt,
    className,
    style
  }
}

type PearlImageProps = {
  image?: PearlImageConfig
  className?: string
  style?: CSSProperties
}

/** renders an image that is part of a SiteContent spec */
export default function PearlImage(props: PearlImageProps) {
  const { image, className, style } = props

  if (!image) {
    return <></>
  }

  return (
    <img
      src={getImageUrl(image.cleanFileName, image.version)}
      alt={image.alt}
      className={classNames('pearl-image', className, image.className)}
      loading="lazy"
      style={{ ...style, ...image.style }}
    />
  )
}
