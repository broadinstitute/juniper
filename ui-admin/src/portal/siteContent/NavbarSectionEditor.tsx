import React, {
  useId,
  useMemo
} from 'react'
import {
  LocalSiteContent,
  NavbarItem,
  NavbarItemGroup,
  NavBarItemType
} from '@juniper/ui-core'
import { CollapsibleSectionButton } from 'portal/siteContent/designer/components/CollapsibleSectionButton'
import { ListElementController } from 'portal/siteContent/designer/components/ListElementController'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import Select from 'react-select'
import { TextInput } from 'components/forms/TextInput'


export function NavbarSectionEditor(
  {
    localSiteContent,
    updateNavbarItems
  } : {
    localSiteContent: LocalSiteContent,
    updateNavbarItems: (navbarItems: NavbarItem[]) => void
  }) {
  const navbarItems = useMemo(() => {
    return localSiteContent.navbarItems || []
  }, [localSiteContent.navbarItems])

  const navbarContentId = useId()
  const navbarContentSelector = `#${navbarContentId}`

  return <>
    <CollapsibleSectionButton
      targetSelector={navbarContentSelector}
      sectionLabel={`Navbar Items (${navbarItems.length})`}/>
    <div className="collapse hide rounded-3 mb-2" id={navbarContentId}
      style={{ backgroundColor: '#eee', padding: '0.75rem' }}>
      <div>
        {navbarItems.map((navItem, i) => (
          <div key={i}>
            <div className="d-flex justify-content-between align-items-center">
              <span className="h6">Edit navbar item</span>
              <ListElementController<NavbarItem>
                index={i}
                items={navbarItems}
                updateItems={newNavItems => {
                  updateNavbarItems(newNavItems)
                }}
              />
            </div>
            <NavbarEditor
              key={i}
              navbarItem={navItem}
              localSiteContent={localSiteContent}
              updateItem={newItem => {
                const newItems = [...navbarItems]
                newItems[i] = newItem
                updateNavbarItems(newItems)
              }}/>
          </div>
        ))}
      </div>
      <Button onClick={() => {
        const newItems: NavbarItem[] = [...navbarItems]
        newItems.push({
          itemType: 'INTERNAL',
          text: '',
          internalPath: '',
          itemOrder: navbarItems.length
        } as NavbarItem)
        updateNavbarItems(newItems)
      }}><FontAwesomeIcon icon={faPlus}/> Add Navbar Item
      </Button>
    </div>
  </>
}

const NavbarTypeOptions: { label: string, value: NavBarItemType }[] = [
  { value: 'INTERNAL', label: 'Internal' },
  { value: 'INTERNAL_ANCHOR', label: 'Internal Anchor' },
  { value: 'MAILING_LIST', label: 'Mailing List' },
  { value: 'EXTERNAL', label: 'External' },
  { value: 'GROUP', label: 'Dropdown' }
]


const NavbarEditor = ({ localSiteContent, navbarItem, updateItem }: {
  localSiteContent: LocalSiteContent,
  navbarItem: NavbarItem,
  updateItem: (item: NavbarItem) => void
}) => {
  const pageOptions = localSiteContent.pages.map(page => ({ value: page.path, label: page.title }))
  return (
    <>
      <div style={{ backgroundColor: '#eee', padding: '0.75rem' }} className="rounded-3 mb-2">
        <label className='form-label fw-semibold mb-2'>Navbar Item Type</label>
        <Select
          aria-label={'Type'}
          placeholder={'Select item type'}
          options={NavbarTypeOptions}
          value={NavbarTypeOptions.find(option => option.value === navbarItem.itemType)}
          onChange={e => {
            if (!e) {
              return
            }

            if (e.value === 'GROUP') {
              updateItem({
                text: navbarItem.text,
                itemOrder: navbarItem.itemOrder,
                itemType: e.value,
                items: []
              } as NavbarItemGroup)
            } else {
              updateItem({
                text: navbarItem.text,
                itemOrder: navbarItem.itemOrder,
                itemType: e.value
              } as NavbarItem)
            }
          }}
        />
        <TextInput label="Text" className="mb-2" value={navbarItem.text} onChange={value => {
          updateItem({ ...navbarItem, text: value })
        }}/>
        {(navbarItem.itemType === 'EXTERNAL' || navbarItem.itemType === 'INTERNAL_ANCHOR') &&
            <TextInput label="Navbar Link" className="mb-2" value={navbarItem.href} onChange={value => {
              updateItem({ ...navbarItem, href: value })
            }}/>}
        {navbarItem.itemType === 'INTERNAL' && <div>
          <label className='form-label fw-semibold mb-2'>Destination</label>
          <Select
            aria-label={'Select Page'}
            placeholder={'Select page'}
            options={pageOptions}
            value={pageOptions.find(option => option.value === navbarItem.internalPath)}
            onChange={e => e && updateItem({ ...navbarItem, internalPath: e.value } as NavbarItem)}
          />
        </div>}

        {navbarItem.itemType === 'GROUP' && <NavbarDropdownEditor
          localSiteContent={localSiteContent}
          navbarItem={navbarItem}
          updateItem={updateItem}
        />}
      </div>
    </>
  )
}


const NavbarDropdownEditor = ({ localSiteContent, navbarItem, updateItem }: {
  localSiteContent: LocalSiteContent,
  navbarItem: NavbarItemGroup,
  updateItem: (item: NavbarItem) => void
}) => {
  return (
    <div style={{ backgroundColor: '#ddd', padding: '0.75rem' }} className="rounded-3 mb-2">
      <label className='form-label fw-semibold mb-2'>Dropdown Items</label>
      {navbarItem.items?.map((groupItem, i) => (
        <div key={i}>
          <div className="d-flex justify-content-between align-items-center">
            <span className="h6">Edit dropdown item</span>
            <ListElementController<NavbarItem>
              index={i}
              items={navbarItem.items}
              updateItems={newItems => {
                updateItem({ ...navbarItem, items: newItems })
              }}
            />
          </div>
          <NavbarEditor
            key={i}
            navbarItem={groupItem}
            localSiteContent={localSiteContent}
            updateItem={newItem => {
              const newItems = [...navbarItem.items]
              newItems[i] = newItem
              updateItem({ ...navbarItem, items: newItems })
            }}/>
        </div>
      ))}
      <Button onClick={() => {
        const newItems: NavbarItem[] = [...navbarItem.items]
        newItems.push({
          itemType: 'INTERNAL',
          text: '',
          internalPath: '',
          itemOrder: navbarItem.items.length
        } as NavbarItem)
        updateItem({ ...navbarItem, items: newItems })
      }}><FontAwesomeIcon icon={faPlus}/> Add Dropdown Item
      </Button>
    </div>
  )
}
