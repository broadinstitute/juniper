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
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faX } from '@fortawesome/free-solid-svg-icons'


export function NavbarEditor({
  siteContent,
  portalEnvContext,
  createNewVersion
} : {
  siteContent: SiteContent
  portalEnvContext: PortalEnvContext
  createNewVersion: (workingContent: SiteContent) => void
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
  return <div className='ms-2'>
    {languageOptions.length > 1 && <div className="ms-2" style={{ width: 200 }}>
      <Select
        options={languageOptions}
        value={selectedLanguageOption}
        inputId={selectLanguageInputId}
        aria-label={'Select a language'}
        onChange={languageOnChange}
      />
    </div>}
    <button
      disabled={navbarItems.some(item => !isValidNavbarItem(item, pages))}
      className='btn btn-primary mt-2'
      onClick={() => {
        createNewVersion({
          ...siteContent,
          localizedSiteContents: siteContent.localizedSiteContents.map(lsc => {
            if (lsc.language === (selectedLanguageOption?.value?.languageCode || defaultLanguage.languageCode)) {
              return {
                ...lsc,
                navbarItems
              }
            }
            return lsc
          })
        })
      }}
    >
      Save
    </button>
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
        updateNavbarItem={newItem => {
          setNavbarItems(items => {
            const newItems = items.slice()
            newItems[idx] = newItem
            return newItems
          })
        }}
        deleteNavbarItem={() => setNavbarItems(items => items.filter((_, i) => i !== idx))}
      />)}
    </div>

    <button
      className='btn btn-primary mt-2'
      onClick={() => {
        setNavbarItems([...navbarItems, {} as NavbarItem])
      }}>
      Add Item
    </button>

  </div>
}

const NavbarTypeOptions: { label: string, value: NavBarItemType }[] = [
  { value: 'INTERNAL', label: 'Internal' },
  { value: 'INTERNAL_ANCHOR', label: 'Internal Anchor' },
  { value: 'MAILING_LIST', label: 'Mailing List' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GROUP', label: 'Dropdown' }
]

const isValidNavbarItem = (item: NavbarItem, pages: HtmlPage[]) => {
  if ((item.text || '').trim() === '') {
    return false
  }

  if (item.itemType === 'INTERNAL') {
    const path = ((item as NavbarItemInternal).htmlPagePath || '').trim()
    if (path === '') {
      return false
    }
    return pages.find(page => page.path === path) !== undefined
  } else if (item.itemType === 'INTERNAL_ANCHOR') {
    return ((item as NavbarItemInternalAnchor).href || '').trim() !== ''
  } else if (item.itemType === 'EXTERNAL') {
    return ((item as NavbarItemExternal).href || '').trim() !== ''
  } else if (item.itemType === 'GROUP') {
    return ((item as NavbarItemGroup).items || []).length > 0
  } else {
    return false
  }
}

const NavbarItemEditor = (
  {
    navbarItem,
    updateNavbarItem,
    pages,
    deleteNavbarItem,
    isPartOfGroup
  } : {
    navbarItem: NavbarItem
    updateNavbarItem: (item: NavbarItem) => void,
    pages: HtmlPage[],
    deleteNavbarItem?: () => void,
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

  return <>
    <div className="d-flex flex-row align-items-end">
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
                updateNavbarItem({ ...navbarItem, itemType: 'GROUP', items: [{}] } as NavbarItemGroup)
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

      {navbarItem.itemType === 'EXTERNAL' && <NavbarItemExternalEditor
        navbarItem={navbarItem as NavbarItemExternal}
        updateNavbarItem={updateNavbarItem}
      />}

      {navbarItem.itemType === 'INTERNAL_ANCHOR' && <NavbarItemInternalAnchorEditor
        navbarItem={navbarItem as NavbarItemInternalAnchor}
        updateNavbarItem={updateNavbarItem}
      />}

      {/* MAILING_LIST type doesn't need any additional configuration. */}

      <button
        className='btn btn-link'
        onClick={deleteNavbarItem}>
        <FontAwesomeIcon icon={faX}/>
      </button>
    </div>

    <div className="ms-4">
      {navbarItem.itemType === 'GROUP' && <NavbarItemGroupEditor
        navbarItem={navbarItem as NavbarItemGroup}
        updateNavbarItem={updateNavbarItem}
        pages={pages}/>}
    </div>

  </>
}

const NavbarItemInternalEditor = (
  {
    navbarItem,
    updateNavbarItem,
    pages
  }: {
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
  return <div className="ms-2">
    {navbarItem.items.map((item, idx) => <NavbarItemEditor
      key={idx}
      pages={pages}
      navbarItem={item}
      isPartOfGroup={true}
      updateNavbarItem={item => {
        const newItems = navbarItem.items.slice()
        newItems[idx] = item
        updateNavbarItem({ ...navbarItem, items: newItems })
      }}
      deleteNavbarItem={() => {
        updateNavbarItem({ ...navbarItem, items: navbarItem.items.filter((_, i) => i !== idx) })
      }}
    />)}
    <button
      className='btn btn-primary mt-2'
      onClick={() => {
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
