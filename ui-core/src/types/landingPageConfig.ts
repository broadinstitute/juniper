export type SiteContent = {
  id: string
  localizedSiteContents: LocalSiteContent[]
  stableId: string
  version: number
  createdAt: number
}

export type LocalSiteContent = {
  language: string
  navbarItems: NavbarItem[]
  pages: HtmlPage[]
  languageTextOverrides: LanguageText[]
  landingPage: HtmlPage
  navLogoCleanFileName: string
  navLogoVersion: number
  footerSection?: HtmlSection
  primaryBrandColor?: string
  dashboardBackgroundColor?: string
}

export type LanguageText = {
  keyName: string
  text: string
  language: string
}

export type HtmlPage = {
  title: string
  path: string
  sections: HtmlSection[]
  minimalNavbar: boolean
}

export type HtmlSection = {
  id: string
  sectionType: SectionType
  anchorRef?: string
  rawContent?: string | null
  sectionConfig?: string | null
  parsedConfig?: object // NOT stored in DB -- this is a temporary place to store the parsed config
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
  | NavbarItemGroup

export type NavBarItemType = 'INTERNAL' | 'INTERNAL_ANCHOR' | 'MAILING_LIST' | 'EXTERNAL' | 'GROUP'

export type BaseNavBarItem = {
  id?: string
  itemType: NavBarItemType
  text: string
  itemOrder: number
}

export type NavbarItemInternal = BaseNavBarItem & {
  itemType: 'INTERNAL'
  internalPath: string
}

export type NavbarItemGroup = BaseNavBarItem & {
  itemType: 'GROUP'
  items: NavbarItem[]
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
