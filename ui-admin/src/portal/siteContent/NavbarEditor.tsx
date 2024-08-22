import React, {
  useMemo,
  useState
} from 'react'
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
import useLanguageSelectorFromParam from 'portal/languages/useLanguageSelector'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCheck,
  faPlus,
  faX
} from '@fortawesome/free-solid-svg-icons'
import {
  ColumnDef,
  getCoreRowModel,
  useReactTable
} from '@tanstack/react-table'
import { isNil } from 'lodash'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons/faTrashCan'
import { basicTableLayout } from 'util/tableUtils'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
  ModalTitle
} from 'react-bootstrap'
import { NavbarPreview } from 'portal/siteContent/NavbarPreview'
import Api from 'api/api'
import { useConfig } from 'providers/ConfigProvider'
import { isEmpty } from 'lodash/fp'


type EditableNavbarItem = Partial<NavbarItem> & { isEditing: boolean }
type NavbarItemRow = NavbarItem | EditableNavbarItem

const isEditable = (row: NavbarItemRow): row is EditableNavbarItem => {
  return !isNil((row as EditableNavbarItem).isEditing)
}

const NavbarTypeOptions: { label: string, value: NavBarItemType }[] = [
  { value: 'INTERNAL', label: 'Internal' },
  { value: 'INTERNAL_ANCHOR', label: 'Internal Anchor' },
  { value: 'MAILING_LIST', label: 'Mailing List' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GROUP', label: 'Dropdown' }
]

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
    portal, portalEnv
  } = portalEnvContext

  const {
    defaultLanguage, languageOnChange, selectedLanguageOption,
    selectLanguageInputId, languageOptions
  } = useLanguageSelectorFromParam()

  const zoneConfig = useConfig()


  const participantLink = Api.getParticipantLink(portalEnv.portalEnvironmentConfig, zoneConfig.participantUiHostname,
    portalEnvContext.portal.shortcode, portalEnv.environmentName)

  const [newNavbarItem, setNewNavbarItem] = useState<EditableNavbarItem>({ isEditing: false })
  const [navbarItemSelectedForDeletion, setNavbarItemSelectedForDeletion] = useState<NavbarItem>()

  const onNewNavbarItemChange = (field: keyof EditableNavbarItem, value: string | boolean) => {
    setNewNavbarItem({
      ...newNavbarItem,
      [field]: value
    })
  }
  const saveNewNavbarItem = async (newNavbarItem: NavbarItem) => {
    const newMappings = [...navbarItems, newNavbarItem]

    setNavbarItems(newMappings)
    setNewNavbarItem({ isEditing: false })
  }

  const localContent = siteContent
    .localizedSiteContents
    .find(lsc => lsc.language === (selectedLanguageOption?.value?.languageCode || defaultLanguage.languageCode))

  const pages = localContent?.pages || []
  const [navbarItems, setNavbarItems] = React.useState<NavbarItem[]>(localContent?.navbarItems || [])


  const columns = useMemo<ColumnDef<NavbarItemRow>[]>(() => [
    {
      header: 'Type',
      accessorKey: 'itemType',
      cell: ({ row }) => {
        const value = row.original.itemType
        if (isEditable(row.original)) {
          return row.original.isEditing && <Select
            aria-label={'New Navbar Item Type'}
            placeholder={'Select item type'}
            options={NavbarTypeOptions}
            value={NavbarTypeOptions.find(option => option.value === value)}
            onChange={e => e && onNewNavbarItemChange('itemType', e.value as NavBarItemType)}
          />
        }
        return value
      }
    },
    {
      header: 'Name',
      accessorKey: 'text',
      cell: ({ row }) => {
        const value = row.original.text
        if (isEditable(row.original)) {
          return row.original.isEditing && <NavbarTextEditor
            key={row.index}
            navbarItem={newNavbarItem as NavbarItem}
            updateNavbarItem={newItem => {
              setNewNavbarItem(old => {
                return { ...old, ...newItem }
              })
            }}/>
        }
        return value
      }
    },
    {
      header: 'Destination',
      accessorKey: 'targetField',
      cell: ({ row }) => {
        if (isEditable(row.original) && row.original.itemType) {
          return row.original.isEditing && <NavbarDestinationEditor
            navbarItem={row.original as NavbarItem}
            updateNavbarItem={newItem => {
              setNewNavbarItem(old => {
                return { ...old, ...newItem }
              })
            }}
            pages={pages}
          />
        }
        return <NavbarDestination navbarItem={row.original as NavbarItem} pages={pages}
          participantUrl={participantLink}/>
      }
    },
    {
      header: 'Actions',
      id: 'actions',
      cell: ({ row }) => {
        if (isEditable(row.original)) {
          if (!row.original.isEditing) {
            return <button
              className='btn btn-primary border-0'
              onClick={() => onNewNavbarItemChange('isEditing', true)}>
              <FontAwesomeIcon icon={faPlus} aria-label={'Create New Answer Mapping'}/>
            </button>
          }

          return <>
            <button
              className='btn btn-success me-2'
              disabled={!isValidNavbarItem(row.original as NavbarItem, pages)}
              onClick={() => {
                const newItem = {
                  ...row.original,
                  isEditing: undefined
                }
                saveNewNavbarItem(newItem as NavbarItem)
              }}>
              <FontAwesomeIcon icon={faCheck} aria-label={'Accept New Answer Mapping'}/>
            </button>
            <button className='btn btn-danger' onClick={() => onNewNavbarItemChange('isEditing', false)}>
              <FontAwesomeIcon icon={faX} aria-label={'Cancel New Answer Mapping'}/>
            </button>
          </>
        }
        return <button className='btn btn-outline-danger border-0' onClick={() => {
          setNavbarItemSelectedForDeletion(row.original as NavbarItem)
        }}>
          <FontAwesomeIcon icon={faTrashCan} aria-label={'Delete Answer Mapping'}/>
        </button>
      }
    }
  ], [newNavbarItem, navbarItems])

  const data = useMemo(
    () => (navbarItems as NavbarItemRow[]).concat(newNavbarItem),
    [navbarItems, newNavbarItem])

  const table = useReactTable<NavbarItemRow>({
    data,
    columns,
    getCoreRowModel: getCoreRowModel()
  })

  // TODO list:
  //  - Add navbar preview
  //  - Add individual navbar item editor
  //  - Add drag'n'drop reordering
  //  - Add navbar group editor (with drag'n'drop)
  return <div className='m-2'>
    <div className="d-flex flex-row">
      {languageOptions.length > 1 && <div className="ms-2" style={{ width: 200 }}>
        <Select
          options={languageOptions}
          value={selectedLanguageOption}
          inputId={selectLanguageInputId}
          aria-label={'Select a language'}
          onChange={languageOnChange}
        />
      </div>}
      <div className="d-flex flex-grow-1 justify-content-end">
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
      </div>

    </div>

    {localContent && <div
      className="w-100 my-4"
    >
      <NavbarPreview portal={portal} portalEnv={portalEnv} localContent={{
        ...localContent,
        navbarItems
      }}/>
    </div>}

    <div className="m-3">
      {basicTableLayout(table, { tdClass: 'col-1' })}
    </div>

    {navbarItemSelectedForDeletion && <DeleteNavbarItemModal
      onCancel={() => setNavbarItemSelectedForDeletion(undefined)}
      onConfirm={() => {
        setNavbarItems(navbarItems.filter(item => item !== navbarItemSelectedForDeletion))
        setNavbarItemSelectedForDeletion(undefined)
      }}
    />}

  </div>
}

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


const NavbarTextEditor = (
  {
    navbarItem,
    updateNavbarItem
  }: {
    navbarItem: NavbarItem,
    updateNavbarItem: (item: NavbarItem) => void
  }
) => {
  const [textValue, setTextValue] = useState(navbarItem.text)


  return <input
    className="form-control"
    placeholder={'Name'}
    disabled={isEmpty(navbarItem.itemType)}
    value={navbarItem.text}
    onChange={e => {
      setTextValue(e.target.value)
    }}
    onBlur={() => {
      updateNavbarItem({ ...navbarItem, text: textValue })
    }}
  />
}

const NavbarDestination = (
  {
    navbarItem,
    pages,
    participantUrl
  }: {
    navbarItem: NavbarItem,
    pages: HtmlPage[]
    participantUrl: string
  }
) => {
  if (navbarItem.itemType === 'INTERNAL') {
    const page = pages.find(page => page.path === (navbarItem as NavbarItemInternal).htmlPagePath)
    if (!page) {
      return <span className={'fst-italic'}>could not find page</span>
    }

    return <a href={`${participantUrl}/${page.path}`}>{page.title}</a>
  } else if (navbarItem.itemType === 'EXTERNAL') {
    const href = (navbarItem as NavbarItemExternal).href
    return <a href={href}>External link</a>
  } else if (navbarItem.itemType === 'INTERNAL_ANCHOR') {
    const href = (navbarItem as NavbarItemInternalAnchor).href
    return <a href={href}>External link</a>
  } else if (navbarItem.itemType === 'MAILING_LIST') {
    return <span>Mailing List</span>
  } else if (navbarItem.itemType === 'GROUP') {
    return <></>
  }
  return <></>
}


const NavbarDestinationEditor = (
  {
    navbarItem,
    updateNavbarItem,
    pages
  }: {
    navbarItem: NavbarItem
    updateNavbarItem: (item: NavbarItem) => void,
    pages: HtmlPage[]
  }
) => {
  return <>
    {navbarItem.itemType === 'INTERNAL' && <NavbarPageSelector
      navbarItem={navbarItem as NavbarItemInternal}
      updateNavbarItem={updateNavbarItem}
      pages={pages}
    />}

    {navbarItem.itemType === 'EXTERNAL' && <NavbarHrefEditor
      navbarItem={navbarItem as NavbarItemExternal}
      updateNavbarItem={updateNavbarItem}
    />}

    {navbarItem.itemType === 'INTERNAL_ANCHOR' && <NavbarHrefEditor
      navbarItem={navbarItem as NavbarItemInternalAnchor}
      updateNavbarItem={updateNavbarItem}
    />}

    {/* MAILING_LIST type doesn't need any additional configuration. */}

    {navbarItem.itemType === 'GROUP' && <NavbarGroupAddNew
      navbarItem={navbarItem as NavbarItemGroup}
      updateNavbarItem={updateNavbarItem}
    />}

  </>
}


const NavbarPageSelector = (
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

  return <Select
    placeholder={'Select a page'}
    value={currentPage && { value: navbarItem.htmlPagePath, label: currentPage.title }}
    options={pages.map(page => ({ value: page.path, label: page.title || 'Landing Page' }))}
    onChange={val => {
      updateNavbarItem({ ...navbarItem, htmlPagePath: val?.value || '' })
    }}
  />
}

const NavbarGroupAddNew = (
  {
    navbarItem,
    updateNavbarItem
  } : {
    navbarItem: NavbarItemGroup,
    updateNavbarItem: (item: NavbarItem) => void
  }
) => {
  // should be "add new item" button
  return <button
    className='btn btn-primary mt-2'
    onClick={() => {
      updateNavbarItem({ ...navbarItem, items: navbarItem.items.concat({} as NavbarItem) })
    }}>
      Add Item
  </button>
  // return <div className="ms-2">
  //   {navbarItem.items.map((item, idx) => <NavbarItemEditor
  //     key={idx}
  //     pages={pages}
  //     navbarItem={item}
  //     isPartOfGroup={true}
  //     updateNavbarItem={item => {
  //       const newItems = navbarItem.items.slice()
  //       newItems[idx] = item
  //       updateNavbarItem({ ...navbarItem, items: newItems })
  //     }}
  //     deleteNavbarItem={() => {
  //       updateNavbarItem({ ...navbarItem, items: navbarItem.items.filter((_, i) => i !== idx) })
  //     }}
  //   />)}
  //   <button
  //     className='btn btn-primary mt-2'
  //     onClick={() => {
  //       updateNavbarItem({ ...navbarItem, items: navbarItem.items.concat({} as NavbarItem) })
  //     }}>
  //     Add Item
  //   </button>
  // </div>
}


const NavbarHrefEditor = (
  {
    navbarItem,
    updateNavbarItem
  } : {
    navbarItem: NavbarItemInternalAnchor | NavbarItemExternal,
    updateNavbarItem: (item: NavbarItem) => void
  }
) => {
  const [textValue, setTextValue] = useState(navbarItem.text)

  return <input
    className="form-control"
    placeholder={'URL'}
    value={navbarItem.href}
    onChange={e => {
      setTextValue(e.target.value)
    }}
    onBlur={() => {
      updateNavbarItem({ ...navbarItem, href: textValue })
    }}
  />
}

const DeleteNavbarItemModal = (
  { onConfirm, onCancel }: { onConfirm: () => void, onCancel: () => void }
) => {
  return <Modal onHide={onCancel} show={true}>
    <ModalHeader>
      <ModalTitle>
        Are you sure you want to delete this navbar item?
      </ModalTitle>
    </ModalHeader>
    <ModalBody>
      This action cannot be undone.
    </ModalBody>
    <ModalFooter>
      <button className='btn btn-danger' onClick={onConfirm}>
        Yes
      </button>
      <button className='btn btn-secondary' onClick={onCancel}>
        No
      </button>
    </ModalFooter>
  </Modal>
}
