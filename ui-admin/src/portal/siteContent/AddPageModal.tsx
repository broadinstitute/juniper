import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { HtmlPage } from '@juniper/ui-core'

const EMPTY_PAGE: HtmlPage = {
  path: '',
  title: '',
  sections: []
}

/** renders a modal that adds a new page to the site */
const AddPageModal = ({ insertNewPage, onDismiss }: {
  insertNewPage: (item: HtmlPage) => void,
  onDismiss: () => void
}) => {
  const [page, setPage] = useState(EMPTY_PAGE)

  const addPage = async () => {
    insertNewPage(page)
    onDismiss()
  }

  const isItemValid = (page: HtmlPage) => {
    return page.path.length > 0 && page.title.length > 0
  }

  const clearFields = () => {
    setPage(EMPTY_PAGE)
  }

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
        <input type="text" size={50} className="form-control mb-3" id="inputPageTitle" value={page.title}
          onChange={event => {
            setPage({ ...page, title: event.target.value })
          }}/>

        <label htmlFor="inputPagePath">Page Path</label>
        <div className="input-group">
          <input
            type="text"
            className="form-control"
            id="inputPagePath"
            value={page.path}
            aria-describedby="path"
            onChange={event => {
              setPage({ ...page, path: event.target.value })
            }}/>
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        disabled={!isItemValid(page)}
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
