export type SiteContent = {
  id: string
  defaultLanguage: string
  localizedSiteContents: LocalSiteContent[]
  stableId: string
  version: number
  createdAt: number
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

type BaseNavBarItem = {
  id?: string
  itemType: 'INTERNAL' | 'INTERNAL_ANCHOR' | 'MAILING_LIST' | 'EXTERNAL'
  text: string
  itemOrder: number
}

export type NavbarItemInternal = BaseNavBarItem & {
  itemType: 'INTERNAL'
  htmlPage: HtmlPage
}

export type NavbarItemInternalAnchor = BaseNavBarItem & {
  itemType: 'INTERNAL_ANCHOR'
  href: string
}

export type NavbarItemMailingList = BaseNavBarItem & {
  itemType: 'MAILING_LIST'
}

export type NavbarItemExternal = BaseNavBarItem & {
  itemType: 'EXTERNAL'
  href: string
}
