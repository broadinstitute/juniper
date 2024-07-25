import classNames from 'classnames'
import React, { CSSProperties } from 'react'
import { useApiContext } from '../../participant/ApiProvider'

import { requireOptionalString, requirePlainObject, requireOptionalNumber }
  from '../../participant/util/validationUtils'
import { SectionProp } from 'src/participant/landing/sections/SectionProp'

export type VideoConfig = {
  videoLink: string
  alt?: string
  className?: string
  style?: CSSProperties
}

export type ImageConfig = {
  cleanFileName: string
  version: number
  alt?: string
  className?: string
  style?: CSSProperties
  link?: string
}

export type MediaConfig = ImageConfig | VideoConfig

export const mediaConfigProps: SectionProp[] = [
  { name: 'cleanFileName', translated: false, subProps: [] },
  { name: 'version', translated: false, subProps: [] },
  { name: 'alt', translated: true, subProps: [] },
  { name: 'className', translated: false, subProps: [] },
  { name: 'style', translated: false, subProps: [] },
  { name: 'link', translated: false, subProps: [] },
  { name: 'videoLink', translated: false, subProps: [] }
]

export const validateMediaConfig = (imageConfig: unknown): MediaConfig => {
  const message = 'Invalid image config'
  const config = requirePlainObject(imageConfig, message)

  const cleanFileName = requireOptionalString(config, 'cleanFileName', message)
  const version = requireOptionalNumber(config, 'version', message)
  const alt = requireOptionalString(config, 'alt', message)
  const className = requireOptionalString(config, 'className', message)
  const videoLink = requireOptionalString(config, 'videoLink', message)
  const link = requireOptionalString(config, 'link', message)
  // Only validate that style is an object. React will handle invalid keys.
  const style = config.style ? requirePlainObject(config.style, `${message}: Invalid style`) : undefined

  return {
    cleanFileName,
    version,
    alt,
    className,
    style,
    link,
    videoLink
  } as MediaConfig
}

type ConfiguredImageProps = {
  media: MediaConfig
  className?: string
  style?: CSSProperties
  allowMobileFullSize?: boolean
}

/** renders an image that is part of a SiteContent spec */
export default function ConfiguredMedia(props: ConfiguredImageProps) {
  const { media, className, style, allowMobileFullSize } = props
  const { getImageUrl } = useApiContext()
  const imgClass = !allowMobileFullSize ? 'cfg-image-confined' : ''
  if ((media as VideoConfig).videoLink) {
    const videoLinkMedia = media as VideoConfig
    const videoAllowed = isVideoLinkAllowed(videoLinkMedia.videoLink)
    return <div style={{ width: '100%', height: '100%', ...style, ...media.style }}
      className={classNames(imgClass, className, media.className)}>
      {videoAllowed && <iframe src={videoLinkMedia.videoLink} frameBorder="0" allowFullScreen={true}
        data-testid="media-iframe" style={{ width: '100%', height: '100%' }}></iframe> }
      {!videoAllowed && <span className="text-danger">Disallowed video source</span> }
    </div>
  }
  const image = <img
    src={getImageUrl((media as ImageConfig).cleanFileName, (media as ImageConfig).version)}
    alt={media.alt}
    className={classNames(imgClass, className, media.className)}
    loading="lazy"
    style={{ ...style, ...media.style }}
  />
  if ((media as ImageConfig).link) {
    return <a href={(media as ImageConfig).link} target="_blank" rel="noreferrer">{image}</a>
  }
  return image
}

const ALLOWED_VIDEO_HOSTS = ['youtube.com', 'youtu.be', 'vimeo.com']

/**  we don't want to enable arbitrary iframe content on our pages */
const isVideoLinkAllowed = (videoLink: string) => {
  try {
    const url = new URL(videoLink)
    return ALLOWED_VIDEO_HOSTS.some(host => url.host.endsWith(host))
  } catch (e) {
    return false
  }
}
