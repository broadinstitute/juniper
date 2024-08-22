import React, {
  useEffect,
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
  faCaretDown,
  faCaretUp,
  faCheck,
  faClockRotateLeft,
  faPlus,
  faX
} from '@fortawesome/free-solid-svg-icons'
import {
  ColumnDef,
  getCoreRowModel,
  getExpandedRowModel,
  Row,
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
import { Button } from 'components/forms/Button'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'
import { Link } from 'react-router-dom'
import SiteContentVersionSelector from 'portal/siteContent/SiteContentVersionSelector'
import { isEmpty } from 'lodash/fp'


type EditableNavbarItem = Partial<NavbarItem> & { isEditing: boolean }
type NavbarItemRow = NavbarItem | EditableNavbarItem

const isRowEditable = (row: NavbarItemRow): row is EditableNavbarItem => {
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
  createNewVersion,
  loadSiteContent,
  switchToVersion
}: {
  siteContent: SiteContent
  portalEnvContext: PortalEnvContext
  createNewVersion: (workingContent: SiteContent) => void
  loadSiteContent: (stableId: string, version: number, language?: string) => void
  switchToVersion: (id: string, stableId: string, version: number) => void

}) {
  const {
    portal, portalEnv
  } = portalEnvContext

  const {
    defaultLanguage, languageOnChange, selectedLanguageOption,
    selectLanguageInputId, languageOptions
  } = useLanguageSelectorFromParam()

  const zoneConfig = useConfig()

  const isEditable = portalEnv.environmentName === 'sandbox'

  const participantLink = Api.getParticipantLink(portalEnv.portalEnvironmentConfig, zoneConfig.participantUiHostname,
    portalEnvContext.portal.shortcode, portalEnv.environmentName)

  const showParticipantView = () => {
    window.open(participantLink, '_blank')
  }

  const [newNavbarItem, setNewNavbarItem] = useState<EditableNavbarItem>({ isEditing: false })

  // map of HTML page paths to their respective pages
  const [newGroupedItems, setNewGroupedItems] = useState<Record<string, EditableNavbarItem>>({})

  const [navbarItemSelectedForDeletion, setNavbarItemSelectedForDeletion] = useState<NavbarItem>()
  const [showVersionSelector, setShowVersionSelector] = useState(false)

  const onNewNavbarItemChange = (field: keyof EditableNavbarItem, value: string | boolean) => {
    setNewNavbarItem({
      ...newNavbarItem,
      [field]: value
    })
  }

  const onNewGroupedNavbarItemChange = (parent: string, field: keyof EditableNavbarItem, value: string | boolean) => {
    setNewGroupedItems(old => {
      return {
        ...old,
        [parent]: {
          ...old[parent],
          [field]: value
        }
      }
    })
  }

  const onRowFieldChange = (row: Row<NavbarItemRow>, field: keyof NavbarItemRow, value: string | boolean) => {
    const parentRow = row.getParentRow()
    if (!isNil(parentRow)) {
      onNewGroupedNavbarItemChange(parentRow.original.text as string, field, value)
    } else {
      onNewNavbarItemChange(field, value)
    }
  }

  const onRowChange = (row: Row<NavbarItemRow>, newRow: NavbarItem) => {
    const parentRow = row.getParentRow()
    if (!isNil(parentRow)) {
      setNewGroupedItems(
        old => {
          return {
            ...old,
            [parentRow.original.text as string]: {
              ...old[parentRow.original.text as string],
              ...newRow
            }
          }
        }
      )
    } else {
      setNewNavbarItem(old => {
        return { ...old, ...newRow }
      })
    }
  }

  const saveNewNavbarItem = (newNavbarItem: NavbarItem) => {
    const newMappings = [...navbarItems, newNavbarItem]

    setNavbarItems(newMappings)
    setNewNavbarItem({ isEditing: false })
  }

  const saveNewGroupedItem = (parent: Row<NavbarItemRow>, newItem: NavbarItem) => {
    setNewGroupedItems(old => {
      delete old[parent.original.text as string]
      return old
    })

    setNavbarItems(() => {
      return navbarItems.map(item => {
        if (item.text === parent.original.text) {
          return {
            ...item,
            items: [
              ...(item as NavbarItemGroup).items,
              newItem
            ]
          } as NavbarItemGroup
        }
        return item
      })
    })
  }

  const localContent = siteContent
    .localizedSiteContents
    .find(lsc => lsc.language === (selectedLanguageOption?.value?.languageCode || defaultLanguage.languageCode))

  const pages = localContent?.pages || []
  const [navbarItems, setNavbarItems] = React.useState<NavbarItem[]>(localContent?.navbarItems || [])


  const columns = useMemo<ColumnDef<NavbarItemRow>[]>(() => [
    {
      id: 'expanded',
      header: '',
      accessorKey: 'expanded',
      cell: ({ row }) => {
        if (row.depth === 0 && !row.getCanExpand()) {
          return <></>
        }
        return <div
          style={{
            paddingLeft: `${row.depth * 2}rem`
          }}
        >
          <button
            className="btn btn-link"
            onClick={() => row.toggleExpanded()}
            disabled={!row.getCanExpand()}
          >
            {row.getIsExpanded() ? <FontAwesomeIcon icon={faCaretUp}/> : <FontAwesomeIcon icon={faCaretDown}/>}
          </button>
        </div>
      }
    },
    {
      header: 'Type',
      accessorKey: 'itemType',
      cell: ({ row }) => {
        const value = row.original.itemType
        if (isRowEditable(row.original)) {
          return row.original.isEditing && <Select
            aria-label={'New Navbar Item Type'}
            placeholder={'Select item type'}
            options={NavbarTypeOptions}
            value={NavbarTypeOptions.find(option => option.value === value)}
            onChange={e => e && onRowFieldChange(row, 'itemType', e.value as NavBarItemType)}
          />
        }
        return NavbarTypeOptions.find(option => option.value === value)?.label
      }
    },
    {
      header: 'Name',
      accessorKey: 'text',
      cell: ({ row }) => {
        const value = row.original.text
        if (isRowEditable(row.original)) {
          return row.original.isEditing && <NavbarTextEditor
            key={row.index}
            navbarItem={row.original as NavbarItem}
            updateNavbarItem={newItem => {
              onRowChange(row, newItem)
            }}/>
        }
        return value
      }
    },
    {
      header: 'Destination',
      accessorKey: 'targetField',
      cell: ({ row }) => {
        if (isRowEditable(row.original) && row.original.itemType) {
          return row.original.isEditing && <NavbarDestinationEditor
            navbarItem={row.original as NavbarItem}
            updateNavbarItem={newItem => {
              onRowChange(row, newItem)
            }}
            pages={pages}
          />
        }
        return <NavbarDestination
          navbarItem={row.original as NavbarItem} pages={pages}
          participantUrl={participantLink}
        />
      }
    },
    {
      header: 'Actions',
      id: 'actions',
      cell: ({ row }) => {
        if (isRowEditable(row.original)) {
          if (!row.original.isEditing) {
            return <button
              className='btn btn-primary border-0'
              onClick={() => {
                onRowFieldChange(row, 'isEditing' as keyof NavbarItemRow, true)
              }}>
              <FontAwesomeIcon icon={faPlus} aria-label={'Create New Answer Mapping'}/>
            </button>
          }

          return <>
            <button
              className='btn btn-success me-2'
              disabled={
                !isValidNavbarItem(row.original as NavbarItem, pages)
                && !navbarItems.some(item => item.text === row.original.text)
              }
              onClick={() => {
                const newItem = {
                  ...row.original,
                  isEditing: undefined
                }
                const parent = row.getParentRow()
                if (!isNil(parent)) {
                  saveNewGroupedItem(parent, newItem as NavbarItem)
                } else {
                  saveNewNavbarItem(newItem as NavbarItem)
                }
              }}>
              <FontAwesomeIcon icon={faCheck} aria-label={'Accept New Answer Mapping'}/>
            </button>
            <button className='btn btn-danger'
              onClick={() => onRowFieldChange(row, 'isEditing' as keyof NavbarItemRow, false)}>
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
  ], [newNavbarItem, navbarItems, newGroupedItems])

  const data = useMemo(
    () => (navbarItems as NavbarItemRow[]).concat(newNavbarItem),
    [navbarItems, newNavbarItem, newGroupedItems])

  const table = useReactTable<NavbarItemRow>({
    data,
    columns,
    getSubRows: (row: NavbarItemRow) => {
      if (row.itemType === 'GROUP') {
        const items: NavbarItemRow[] = (row as NavbarItemGroup).items
        if (row.text && Object.hasOwn(newGroupedItems, row.text)) {
          return items.concat(newGroupedItems[row.text])
        } else {
          return items.concat({ isEditing: false })
        }
      }
      return []
    },
    enableExpanding: true,
    getExpandedRowModel: getExpandedRowModel(),
    getCoreRowModel: getCoreRowModel()
  })

  useEffect(() => {
    table.toggleAllRowsExpanded(true)
  }, [])

  return <div className='m-2'>
    <div className="d-flex p-2">
      <div className="d-flex flex-grow-1 align-items-center">
        <h5>Navbar
          <span className="fs-6 text-muted fst-italic me-2 ms-2">
            (v{siteContent.version})
          </span>
          {isEditable && <button className="btn btn-secondary"
            onClick={() => setShowVersionSelector(!showVersionSelector)}>
            <FontAwesomeIcon icon={faClockRotateLeft}/> History
          </button>}
          <Button variant="secondary" className="ms-5" onClick={() => showParticipantView()}>
            Participant view <FontAwesomeIcon icon={faExternalLink}/>
          </Button>
        </h5>
      </div>
      <div className="d-flex flex-grow-1 justify-content-end align-items-center">
        {
          isEditable && <>
            <Button
              className="me-md-2"
              variant="primary"
              disabled={navbarItems.some(item => !isValidNavbarItem(item, pages))}
              onClick={() => {
                createNewVersion({
                  ...siteContent,
                  localizedSiteContents: siteContent.localizedSiteContents.map(lsc => {
                    if (lsc.language === (
                      selectedLanguageOption?.value?.languageCode
                            || defaultLanguage.languageCode)
                    ) {
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
            </Button>
            {
              // eslint-disable-next-line
              // @ts-ignore  Link to type also supports numbers for back operations
              <Link className="btn btn-cancel" to={-1}>Cancel</Link>
            }
          </>
        }
      </div>
    </div>


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

    </div>


    {
      localContent && <div
        className="w-100 my-4"
      >
        <NavbarPreview portal={portal} portalEnv={portalEnv} localContent={{
          ...localContent,
          navbarItems
        }}/>
      </div>
    }

    <div className="m-3">
      {basicTableLayout(table, { tdClass: 'col-1' })}
    </div>

    {
      navbarItemSelectedForDeletion && <DeleteNavbarItemModal
        onCancel={() => setNavbarItemSelectedForDeletion(undefined)}
        onConfirm={() => {
          setNavbarItems(navbarItems.filter(item => item !== navbarItemSelectedForDeletion))
          setNavbarItemSelectedForDeletion(undefined)
        }}
      />
    }

    {showVersionSelector &&
        <SiteContentVersionSelector portalShortcode={portalEnvContext.portal.shortcode}
          stableId={siteContent.stableId} current={siteContent}
          loadSiteContent={loadSiteContent} portalEnv={portalEnv}
          switchToVersion={switchToVersion}
          onDismiss={() => setShowVersionSelector(false)}/>
    }

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
    pages: HtmlPage[],
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

    {/*  Mailing list and group types don't need any more info at time of creation*/}

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


const NavbarHrefEditor = (
  {
    navbarItem,
    updateNavbarItem
  }: {
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
