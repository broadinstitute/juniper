import React from 'react'
import {
  HtmlPage,
  NavbarItem,
  NavbarItemExternal,
  NavbarItemGroup,
  NavbarItemInternal,
  NavbarItemInternalAnchor,
  NavBarItemType,
  SiteContent
} from '@juniper/ui-core'
import { PortalEnvContext } from 'portal/PortalRouter'
import { NavbarPreview } from 'portal/siteContent/NavbarPreview'
import useLanguageSelectorFromParam from 'portal/languages/useLanguageSelector'
import Select from 'react-select'


export function NavbarEditor({
  siteContent,
  portalEnvContext,
  isPartOfGroup
} : {
  siteContent: SiteContent
  portalEnvContext: PortalEnvContext
  isPartOfGroup?: boolean
}) {
  const {
    defaultLanguage, languageOnChange, selectedLanguageOption,
    selectLanguageInputId, languageOptions
  } = useLanguageSelectorFromParam()

  const {
    portal,
    portalEnv
  } = portalEnvContext


  const localContent = siteContent
    .localizedSiteContents
    .find(lsc => lsc.language === (selectedLanguageOption?.value?.languageCode || defaultLanguage.languageCode))

  const pages = localContent?.pages || []
  const [navbarItems, setNavbarItems] = React.useState<NavbarItem[]>(localContent?.navbarItems || [])

  // TODO list:
  //  - Add navbar preview
  //  - Add individual navbar item editor
  //  - Add drag'n'drop reordering
  //  - Add navbar group editor (with drag'n'drop)
  return <div>
    { languageOptions.length > 1 && <div className="ms-2" style={{ width: 200 }}>
      <Select
        options={languageOptions}
        value={selectedLanguageOption}
        inputId={selectLanguageInputId}
        aria-label={'Select a language'}
        onChange={languageOnChange}
      />
    </div> }
    {localContent
      ? <NavbarPreview portal={portal} portalEnv={portalEnv} localContent={{
        ...localContent,
        navbarItems
      }}/>
      : <p>Not configured for this language</p>}

    <div>
      {navbarItems.map((item, idx) => <NavbarItemEditor
        key={idx}
        pages={pages}
        navbarItem={item}
        isPartOfGroup={isPartOfGroup}
        updateNavbarItem={newItem => {
          setNavbarItems(items => {
            const newItems = items.slice()
            newItems[idx] = newItem
            return newItems
          })
        }}
      />)}
    </div>
  </div>
}


const NavbarTypeOptions: {label: string, value: NavBarItemType}[] = [
  { value: 'INTERNAL', label: 'Internal' },
  { value: 'INTERNAL_ANCHOR', label: 'Internal Anchor' },
  { value: 'MAILING_LIST', label: 'Mailing List' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GROUP', label: 'Group' }
]

const NavbarItemEditor = (
  {
    navbarItem,
    updateNavbarItem,
    pages,
    isPartOfGroup
  } : {
    navbarItem: NavbarItem
    updateNavbarItem: (item: NavbarItem) => void,
    pages: HtmlPage[],
    isPartOfGroup?: boolean
  }
) => {
  const options = NavbarTypeOptions.filter(opt => {
    // can't have a group inside a group
    if (isPartOfGroup) {
      return opt.value !== 'GROUP'
    }
    return true
  })

  return <div className="d-flex flex-row">
    <div className="me-2">
      <label>Type</label>
      <Select
        value={options.find(opt => opt.value === navbarItem.itemType)}
        options={options}
        onChange={val => {
          switch (val?.value) {
            case 'INTERNAL':
              updateNavbarItem({ ...navbarItem, itemType: 'INTERNAL', htmlPagePath: '' } as NavbarItemInternal)
              break
            case 'INTERNAL_ANCHOR':
              updateNavbarItem({ ...navbarItem, itemType: 'INTERNAL_ANCHOR', href: '' } as NavbarItemInternalAnchor)
              break
            case 'MAILING_LIST':
              updateNavbarItem({ ...navbarItem, itemType: 'MAILING_LIST' })
              break
            case 'EXTERNAL':
              updateNavbarItem({ ...navbarItem, itemType: 'EXTERNAL', href: '' } as NavbarItemExternal)
              break
            case 'GROUP':
              updateNavbarItem({ ...navbarItem, itemType: 'GROUP', items: [] } as NavbarItemGroup)
              break
          }
        }}/>
    </div>
    <div className="me-2">
      <label>Text</label>
      <input className="form-control " value={navbarItem.text} onChange={e => {
        updateNavbarItem({ ...navbarItem, text: e.target.value })
      }}/>
    </div>

    {navbarItem.itemType === 'INTERNAL' && <NavbarItemInternalEditor
      navbarItem={navbarItem as NavbarItemInternal}
      updateNavbarItem={updateNavbarItem}
      pages={pages}
    />}

    {navbarItem.itemType === 'GROUP' && <NavbarItemGroupEditor
      navbarItem={navbarItem as NavbarItemGroup}
      updateNavbarItem={updateNavbarItem}
      pages={pages}/>}

    {navbarItem.itemType === 'EXTERNAL' && <NavbarItemExternalEditor
      navbarItem={navbarItem as NavbarItemExternal}
      updateNavbarItem={updateNavbarItem}
    />}

    {navbarItem.itemType === 'INTERNAL_ANCHOR' && <NavbarItemInternalAnchorEditor
      navbarItem={navbarItem as NavbarItemInternalAnchor}
      updateNavbarItem={updateNavbarItem}
    />}

    {/* MAILING_LIST type doesn't need any additional configuration. */}

  </div>
}

const NavbarItemInternalEditor = (
  {
    navbarItem,
    updateNavbarItem,
    pages
  } : {
    navbarItem: NavbarItemInternal,
    updateNavbarItem: (item: NavbarItem) => void,
    pages: HtmlPage[]
  }
) => {
  const currentPage = pages.find(page => page.path === navbarItem.htmlPagePath)

  return <>
    <div>
      <label>Page</label>
      <Select
        value={currentPage && { value: navbarItem.htmlPagePath, label: currentPage.title }}
        options={pages.map(page => ({ value: page.path, label: page.title || 'Landing Page' }))}
        onChange={val => {
          updateNavbarItem({ ...navbarItem, htmlPagePath: val?.value || '' })
        }}
      />
    </div>
  </>
}

const NavbarItemGroupEditor = (
  {
    navbarItem,
    updateNavbarItem,
    pages
  } : {
    navbarItem: NavbarItemGroup,
    updateNavbarItem: (item: NavbarItem) => void,
    pages: HtmlPage[]
  }
) => {
  return <div className="ms-3">
    <p>Sub items</p>
    {navbarItem.items.map((item, idx) => <NavbarItemEditor
      key={idx}
      pages={pages}
      navbarItem={item}
      updateNavbarItem={item => {
        const newItems = navbarItem.items.slice()
        newItems[idx] = item
        updateNavbarItem({ ...navbarItem, items: newItems })
      }}
    />)}
    <button onClick={() => {
      updateNavbarItem({ ...navbarItem, items: navbarItem.items.concat({} as NavbarItem) })
    }}>
      Add Item
    </button>
  </div>
}

const NavbarItemExternalEditor = (
  {
    navbarItem,
    updateNavbarItem
  } : {
    navbarItem: NavbarItemExternal,
    updateNavbarItem: (item: NavbarItem) => void
  }
) => {
  return <>
    <div>
      <label>URL</label>
      <input className="form-control" value={navbarItem.href} onChange={e => {
        updateNavbarItem({ ...navbarItem, href: e.target.value })
      }}/>
    </div>
  </>
}

const NavbarItemInternalAnchorEditor = (
  {
    navbarItem,
    updateNavbarItem
  } : {
    navbarItem: NavbarItemInternalAnchor,
    updateNavbarItem: (item: NavbarItem) => void
  }
) => {
  return <>
    <div>
      <label>URL</label>
      <input className="form-control" value={navbarItem.href} onChange={e => {
        updateNavbarItem({ ...navbarItem, href: e.target.value })
      }}/>
    </div>
  </>
}
