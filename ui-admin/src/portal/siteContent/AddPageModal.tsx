import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { BaseNavBarItem, NavBarItemType, PortalEnvironment } from '@juniper/ui-core'
import Api from 'api/api'
import { useConfig } from 'providers/ConfigProvider'
import { useNonNullReactSingleSelect } from '../../util/react-select-utils'
import Select from 'react-select'

const ITEM_TYPE_OPTIONS: {label: string, value: NavBarItemType}[] = [{
  label: 'Page', value: 'INTERNAL'
}, {
  label: 'External link', value: 'EXTERNAL'
}]

export type NavItemProps = BaseNavBarItem & {href: string}

const EMPTY_NAV_ITEM: NavItemProps = {
  text: '',
  itemType: 'INTERNAL',
  href: '',
  itemOrder: -1
}

/** renders a modal that adds a new page to the site */
const AddPageModal = ({ portalEnv, portalShortcode, insertNewNavItem, onDismiss }: {
  portalEnv: PortalEnvironment, portalShortcode: string, insertNewNavItem: (item: NavItemProps) => void,
  onDismiss: () => void
}) => {
  const zoneConfig = useConfig()
  const [navbarItem, setNavbarItem] = useState(EMPTY_NAV_ITEM)

  const {
    selectInputId, selectedOption,
    options: itemTypeOptions, onChange: onItemTypeChange
  } = useNonNullReactSingleSelect<NavBarItemType>(
    ITEM_TYPE_OPTIONS.map(opt => opt.value),
    itemTypeOpt => ITEM_TYPE_OPTIONS.find(opt => opt.value === itemTypeOpt)!,
    (opt: NavBarItemType) => setNavbarItem({ ...navbarItem, itemType: opt }),
    navbarItem.itemType)

  const addPage = async () => {
    insertNewNavItem(navbarItem)
    onDismiss()
  }

  const isItemValid = (navbarItem: NavItemProps) => {
    return navbarItem.text && navbarItem.itemType && navbarItem.href
  }

  const clearFields = () => {
    setNavbarItem(EMPTY_NAV_ITEM)
  }

  const portalUrl = Api.getParticipantLink(portalEnv.portalEnvironmentConfig, zoneConfig.participantUiHostname,
    portalShortcode, portalEnv.environmentName)

  return <Modal show={true} onHide={() => {
    clearFields()
    onDismiss()
  }}>
    <Modal.Header closeButton>
      <Modal.Title>Add New Page</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label htmlFor="inputPageTitle">Page Title</label>
        <input type="text" size={50} className="form-control mb-3" id="inputPageTitle" value={navbarItem.text}
          onChange={event => {
            setNavbarItem({ ...navbarItem, text: event.target.value })
          }}/>
        <div className="mb-3">
          <label htmlFor={selectInputId}>Navbar item type</label>
          <Select options={itemTypeOptions} value={selectedOption} inputId={selectInputId}
            aria-label={'Select the navbar item type'}
            onChange={onItemTypeChange}/>
        </div>
        { navbarItem.itemType === 'EXTERNAL' && <div>
          <label htmlFor="inputPagePath">Link</label>
          <input type="text" className="form-control" id="inputPageHref"
            value={navbarItem.href}
            onChange={event => {
              setNavbarItem({ ...navbarItem, href: event.target.value })
            }}/>
        </div>}
        { navbarItem.itemType === 'INTERNAL' && <div>
          <label htmlFor="inputPagePath">Page Path</label>
          <div className="input-group">
            <div className="input-group-prepend">
              <span className="input-group-text" style={{ borderTopRightRadius: 0, borderBottomRightRadius: 0 }}
                id="pathPrefix">{portalUrl}/</span>
            </div>
            <input type="text" className="form-control" id="inputPagePath"
              value={navbarItem.href} aria-describedby="pathPrefix"
              onChange={event => {
                setNavbarItem({ ...navbarItem, href: event.target.value })
              }}/>
          </div>
        </div>
        }
      </form>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        disabled={!isItemValid(navbarItem)}
        onClick={addPage}
      >Create</button>
      <button className="btn btn-secondary" onClick={() => {
        onDismiss()
        clearFields()
      }}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default AddPageModal
