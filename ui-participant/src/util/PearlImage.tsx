import React, { CSSProperties } from 'react'
import { getImageUrl } from 'api/api'

export type ArborImageProps = {
  imageShortcode?: string,
  alt?: string,
  className?: string
  style?: CSSProperties
}

/** renders an image that is part of a SiteContent spec */
export default function PearlImage({ imageShortcode, alt, ...rest }: ArborImageProps) {
  if (!imageShortcode) {
    return <></>
  }
  return <img src={getImageUrl(imageShortcode)} alt={alt} loading="lazy" {...rest} />
}
