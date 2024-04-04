import { CSSProperties } from 'react'
import { cssVar, parseToRgb, tint } from 'polished'

export type BrandConfiguration = {
  brandColor?: string;
  backgroundColor?: string;
}

/**
 * Provides brand-specific styling.
 */
export const brandStyles = (config: BrandConfiguration): CSSProperties => {
  const brandColor = config.brandColor || cssVar('--bs-blue') as string
  const backgroundColor = config.backgroundColor || '#fff'
  const brandColorRgb = parseToRgb(brandColor)

  return {
    // Custom properties used in index.css.
    '--brand-color': brandColor,
    '--dashboard-background-color': backgroundColor,
    '--brand-color-rgb': `${brandColorRgb.red}, ${brandColorRgb.green}, ${brandColorRgb.blue}`,
    '--brand-color-contrast': '#fff',
    '--brand-color-shift-10': tint(0.10, brandColor),
    '--brand-color-shift-15': tint(0.15, brandColor),
    '--brand-color-shift-20': tint(0.20, brandColor),
    '--brand-color-shift-90': tint(0.90, brandColor),
    '--brand-link-color': brandColor,
    // Override Bootstrap properties.
    '--bs-link-color': brandColor,
    '--bs-link-hover-color': tint(0.20, brandColor)
  } as CSSProperties
}
