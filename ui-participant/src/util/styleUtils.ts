import { CSSProperties } from 'react'

import { getImageUrl, SectionConfig } from 'api/api'
import { isPlainObject } from 'util/validationUtils'

const allowedStyles = [
  'background',
  'backgroundColor',
  'color'
] as const

/** From section configuration, get styles to apply to the section's container */
export const getSectionStyle = (config: SectionConfig): CSSProperties => {
  const style: CSSProperties = allowedStyles.reduce(
    (acc, property) => config[property]
      ? { ...acc, [property]: config[property] }
      : acc,
    {}
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
