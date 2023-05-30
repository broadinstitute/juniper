export type SiteContent = {
  defaultLanguage: string
  localizedSiteContents: LocalSiteContent[]
  stableId: string
  version: number
}

export type LocalSiteContent = {
  language: string
  navbarItems: NavbarItem[]
  landingPage: HtmlPage
  navLogoCleanFileName: string
  navLogoVersion: number
  footerSection?: HtmlSection
  primaryBrandColor?: string
}

export type HtmlPage = {
  title: string
  path: string
  sections: HtmlSection[]
}

export type HtmlSection = {
  id: string
  sectionType: SectionType
  anchorRef?: string
  rawContent?: string | null
  sectionConfig?: string | null
}

// Type for JSON decoded HtmlSection sectionConfig.
export type SectionConfig = Record<string, unknown>

export type SectionType =
  | 'BANNER_IMAGE'
  | 'FAQ'
  | 'HERO_CENTERED'
  | 'HERO_WITH_IMAGE'
  | 'LINK_SECTIONS_FOOTER'
  | 'PARTICIPATION_DETAIL'
  | 'PHOTO_BLURB_GRID'
  | 'RAW_HTML'
  | 'SOCIAL_MEDIA'
  | 'STEP_OVERVIEW'

export type NavbarItem =
  | NavbarItemInternal
  | NavbarItemInternalAnchor
  | NavbarItemMailingList
  | NavbarItemExternal

export type NavbarItemInternal = {
  itemType: 'INTERNAL'
  text: string
  htmlPage: HtmlPage
}

export type NavbarItemInternalAnchor = {
  itemType: 'INTERNAL_ANCHOR'
  text: string
  href: string
}

export type NavbarItemMailingList = {
  itemType: 'MAILING_LIST'
  text: string
}

export type NavbarItemExternal = {
  itemType: 'EXTERNAL'
  text: string
  href: string
}
