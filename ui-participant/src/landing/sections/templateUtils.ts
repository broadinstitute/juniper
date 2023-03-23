import { SectionConfig } from 'api/api'

export type TemplateComponentProps<ConfigType extends SectionConfig = SectionConfig> = {
  anchorRef?: string
  config: ConfigType
  rawContent: string | null
}

export type TemplateComponent<ConfigType extends SectionConfig = SectionConfig> = (
  props: TemplateComponentProps<ConfigType>
) => JSX.Element | null
