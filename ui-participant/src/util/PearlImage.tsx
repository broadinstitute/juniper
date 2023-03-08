import React, { CSSProperties } from 'react'
import { getImageUrl } from 'api/api'

export type PearlImageConfig = {
  cleanFileName: string,
  version: number,
  alt?: string,
  className?: string
  style?: CSSProperties
}

type PearlImageProps = {
  image?: PearlImageConfig
  className?: string
  style?: CSSProperties
}

/** renders an image that is part of a SiteContent spec */
export default function PearlImage(props: PearlImageProps) {
  const { image, className, style } = props
  const combinedClassNames = `${className ?? ''} ${image?.className ?? ''}`
  if (!image) {
    return <></>
  }
  return <img src={getImageUrl(image.cleanFileName, image.version)} alt={image.alt} loading="lazy"
    className={combinedClassNames} style={{ ...image.style, ...style }}/>
}
