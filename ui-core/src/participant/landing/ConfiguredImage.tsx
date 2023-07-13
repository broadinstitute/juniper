import classNames from 'classnames'
import React, { CSSProperties } from 'react'
import { useApiContext } from '../../participant/ApiProvider'

import { requireOptionalString, requireNumber, requirePlainObject, requireString }
  from '../../participant/util/validationUtils'

export type ImageConfig = {
  cleanFileName: string,
  version: number,
  alt?: string,
  className?: string
  style?: CSSProperties
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const validateImageConfig = (imageConfig: unknown): ImageConfig => {
  const message = 'Invalid image config'
  const config = requirePlainObject(imageConfig, message)

  const cleanFileName = requireString(config, 'cleanFileName', message)
  const version = requireNumber(config, 'version', message)
  const alt = requireOptionalString(config, 'alt', message)
  const className = requireOptionalString(config, 'className', message)

  // Only validate that style is an object. React will handle invalid keys.
  const style = config.style ? requirePlainObject(config.style, `${message}: Invalid style`) : undefined

  return {
    cleanFileName,
    version,
    alt,
    className,
    style
  }
}

type ConfiguredImageProps = {
  image: ImageConfig
  className?: string
  style?: CSSProperties
}

/** renders an image that is part of a SiteContent spec */
export default function ConfiguredImage(props: ConfiguredImageProps) {
  const { image, className, style } = props
  const { getImageUrl } = useApiContext()
  return (
    <img
      src={getImageUrl(image.cleanFileName, image.version)}
      alt={image.alt}
      className={classNames('configured-image', className, image.className)}
      loading="lazy"
      style={{ ...style, ...image.style }}
    />
  )
}
