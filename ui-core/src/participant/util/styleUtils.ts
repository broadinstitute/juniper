import { CSSProperties } from 'react'

import { SectionConfig } from '../../types/landingPageConfig'
import { isPlainObject } from './validationUtils'
import { ImageUrlFunc } from '../../participant/ApiProvider'

const allowedStyles = [
  'background',
  'backgroundColor',
  'color',
  'paddingBottom',
  'paddingTop'
] as const

export const sectionStyleConfigKeys = [...allowedStyles, 'backgroundImage'] as const

/**
 * Given a section config, return an style object with only the allowed styles.
 */
export const applyAllowedStyles = (config: SectionConfig, defaultStyles = {}): CSSProperties => {
  return allowedStyles.reduce(
    (acc, property) => Object.hasOwn(config, property)
      ? { ...acc, [property]: config[property] }
      : acc,
    defaultStyles
  )
}

/** From section configuration, get styles to apply to the section's container */
export const getSectionStyle = (config: SectionConfig, getImageUrl: ImageUrlFunc): CSSProperties => {
  const defaultStyles = {
    paddingBottom: '3rem',
    paddingTop: '3rem'
  }

  const style: CSSProperties = applyAllowedStyles(config, defaultStyles)

  // backgroundImage is not a pass-through style, so must be handled separately.
  if (isPlainObject(config.backgroundImage)) {
    const { cleanFileName, version } = config.backgroundImage
    if (typeof cleanFileName === 'string' && typeof version === 'number') {
      style.backgroundImage = `url('${getImageUrl(cleanFileName, version)}')`
    }
  }

  return style
}
