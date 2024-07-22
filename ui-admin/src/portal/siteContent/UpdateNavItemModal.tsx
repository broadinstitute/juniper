import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { NavbarItem, PortalEnvironment } from '@juniper/ui-core'
import InfoPopup from '../../components/forms/InfoPopup'
import Api from '../../api/api'
import { useConfig } from '../../providers/ConfigProvider'

/** renders a modal for renaming pages of the portal website */
const UpdateNavItemModal = ({ portalEnv, portalShortcode, navItem, updateNavItem, onDismiss }: {
  portalEnv: PortalEnvironment, portalShortcode: string,
  navItem: NavbarItem, updateNavItem: (navItem: NavbarItem) => void, onDismiss: () => void
}) => {
  const zoneConfig = useConfig()
  const [newTitle, setNewTitle] = useState(navItem.text)
  const [newPath, setNewPath] = useState(navItem.itemType === 'INTERNAL' ? navItem.htmlPage.path : '')

  const portalUrl = Api.getParticipantLink(portalEnv.portalEnvironmentConfig, zoneConfig.participantUiHostname,
    portalShortcode, portalEnv.environmentName)

  return <Modal show={true}
    onHide={() => {
      onDismiss()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Edit Page Configuration</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        <div className="mb-3">
          <label htmlFor="inputPageTitle">Page Title</label>
          <input
            type="text"
            id="inputPageTitle"
            className="form-control"
            value={newTitle}
            onChange={e => {
              setNewTitle(e.target.value)
            }}
          />
        </div>
        { navItem.itemType === 'INTERNAL' && <>
          <label htmlFor="inputPagePath">Page Path
            <InfoPopup title="Page Path" content={
              <div>
                The path to the page within your portal. For example, a path of&nbsp;
                <code>my-path</code> will be available at the URL:&nbsp;
                <br/><br/>
                <code>{portalUrl}/my-path</code>.
              </div>
            }/>
          </label>
          <input
            type="text"
            id="inputPagePath"
            className="form-control"
            value={newPath}
            onChange={e => {
              setNewPath(e.target.value)
            }}/>
        </>}
      </div>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        onClick={() => {
          if (navItem.itemType === 'INTERNAL') {
            updateNavItem({
              ...navItem,
              text: newTitle,
              htmlPage: {
                ...navItem.htmlPage,
                title: newTitle,
                path: newPath
              }
            })
          }
          onDismiss()
        }}
      >Update
      </button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default UpdateNavItemModal
