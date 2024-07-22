import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { NavbarItem } from '@juniper/ui-core'

/** renders a modal for renaming pages of the portal website */
const UpdateNavItemModal = ({ navItem, updateNavItem, onDismiss }: {
  navItem: NavbarItem, updateNavItem: (navItem: NavbarItem) => void, onDismiss: () => void
}) => {
  const [newName, setNewName] = useState(navItem.text)
  const [newPath, setNewPath] = useState(navItem.itemType === 'INTERNAL' ? navItem.htmlPage.path : '')

  return <Modal show={true}
    onHide={() => {
      onDismiss()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Rename Page</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        <label htmlFor="renameNavItemInput">Name<strong>{navItem.text}</strong>:</label>
        <input
          type="text"
          id="renameNavItemInput"
          className="form-control"
          value={newName}
          onChange={e => {
            setNewName(e.target.value)
          }}
        />
        { navItem.itemType === 'INTERNAL' && <>
          <label className='pt-3' htmlFor="renameNavItemPathInput">New path for <strong>{navItem.text}</strong>:</label>
          <input
            type="text"
            id="renameNavItemPathInput"
            className="form-control"
            value={newPath}
            onChange={e => {
              setNewPath(e.target.value)
            }}
          />
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
              text: newName,
              htmlPage: {
                ...navItem.htmlPage,
                title: newName,
                path: newPath
              }
            })
          } else {
            updateNavItem({
              ...navItem,
              text: newName
            })
          }
          onDismiss()
        }}
      >Rename
      </button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default UpdateNavItemModal
