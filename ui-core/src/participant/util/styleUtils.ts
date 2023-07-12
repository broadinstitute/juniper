import { CSSProperties } from 'react'

import { getImageUrl, SectionConfig } from 'api/api'
import { isPlainObject } from 'util/validationUtils'

const allowedStyles = [
  'background',
  'backgroundColor',
  'color',
  'paddingBottom',
  'paddingTop'
] as const

export const sectionStyleConfigKeys = [...allowedStyles, 'backgroundImage'] as const

/** From section configuration, get styles to apply to the section's container */
export const getSectionStyle = (config: SectionConfig): CSSProperties => {
  const defaultStyles = {
    paddingBottom: '3rem',
    paddingTop: '3rem'
  }

  const style: CSSProperties = allowedStyles.reduce(
    (acc, property) => Object.hasOwn(config, property)
      ? { ...acc, [property]: config[property] }
      : acc,
    defaultStyles
  )

  // backgroundImage is not a pass-through style, so must be handled separately.
  if (isPlainObject(config.backgroundImage)) {
    const { cleanFileName, version } = config.backgroundImage
    if (typeof cleanFileName === 'string' && typeof version === 'number') {
      style.backgroundImage = `url('${getImageUrl(cleanFileName, version)}')`
    }
  }

  return style
}
