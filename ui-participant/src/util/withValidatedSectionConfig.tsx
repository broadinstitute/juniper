import { pick } from 'lodash'
import React, { ComponentType } from 'react'

import { SectionConfig } from 'api/api'
import { sectionStyleConfigKeys } from 'util/styleUtils'

/** Returns a higher order component that accepts section configuration, validates that it
 *  conforms to ConfigType, and renders the wrapped component with the validated configuration.
 *  If the section configuration is not valid, validatedSectionConfig should throw an error
 *  and the returned component will render nothing.
 */
export const withValidatedSectionConfig = <ConfigType, TemplatePropsType extends { config: ConfigType }>(
  validateSectionConfig: (inputConfig: SectionConfig) => ConfigType,
  Component: ComponentType<TemplatePropsType>
) => {
  type WrapperPropsType = Omit<TemplatePropsType, 'config'> & { config: SectionConfig }
  const Wrapper = (props: WrapperPropsType) => {
    const { config, ...otherProps } = props
    try {
      const validConfig: ConfigType = {
        // pass through section style configuration
        ...pick(config, sectionStyleConfigKeys),
        ...validateSectionConfig(config)
      }
      return <Component {...(otherProps as TemplatePropsType)} config={validConfig} />
    } catch (err: unknown) {
      console.warn(`Invalid section config: ${err instanceof Error ? err.message : err}`)
      return null
    }
  }
  return Wrapper
}
