export type SectionProp = {
  name: string
  translated?: boolean  // whether this prop represents text that is read by a participant, and thus needs i18n support
  subProps?: SectionProp[]
  isArray?: boolean
}

export const titleProp: SectionProp = {
  name: 'title',
  translated: true
}

export const blurbProp: SectionProp = {
  name: 'blurb',
  translated: true
}

export const textProp: SectionProp = {
  name: 'text',
  translated: true
}
