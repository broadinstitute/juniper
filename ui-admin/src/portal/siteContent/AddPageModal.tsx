import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { HtmlPage, PortalEnvironment } from '@juniper/ui-core'
import Api from 'api/api'
import { useConfig } from 'providers/ConfigProvider'

/** renders a modal that adds a new page to the site */
const AddPageModal = ({ portalEnv, portalShortcode, insertNewPage, show, setShow }: {
  portalEnv: PortalEnvironment, portalShortcode: string, insertNewPage: (page: HtmlPage) => void,
  show: boolean, setShow:  React.Dispatch<React.SetStateAction<boolean>>
}) => {
  const zoneConfig = useConfig()

  const [pageTitle, setPageTitle] = useState('')
  const [pagePath, setPagePath] = useState('')

  const addPage = async () => {
    insertNewPage({
      title: pageTitle,
      path: pagePath,
      sections: []
    })
    setShow(false)
  }

  const clearFields = () => {
    setPageTitle('')
    setPagePath('')
  }

  const portalUrl = Api.getParticipantLink(portalEnv.portalEnvironmentConfig, zoneConfig.participantUiHostname,
    portalShortcode, portalEnv.environmentName)

  return <Modal show={show}
    onHide={() => {
      setShow(false)
      clearFields()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Add New Page</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <label htmlFor="inputPageTitle">Page Title</label>
        <input type="text" size={50} className="form-control mb-3" id="inputPageTitle" value={pageTitle}
          onChange={event => {
            setPageTitle(event.target.value)
          }}/>
        <label htmlFor="inputPagePath">Page Path</label>
        <div className="input-group">
          <div className="input-group-prepend">
            <span className="input-group-text" style={{ borderTopRightRadius: 0, borderBottomRightRadius: 0 }}
              id="pathPrefix">{portalUrl}/</span>
          </div>
          <input type="text" className="form-control" id="inputPagePath"
            value={pagePath} aria-describedby="pathPrefix"
            onChange={event => {
              setPagePath(event.target.value)
            }}/>
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <button
        className="btn btn-primary"
        disabled={!pageTitle || !pagePath}
        onClick={addPage}
      >Create</button>
      <button className="btn btn-secondary" onClick={() => {
        setShow(false)
        clearFields()
      }}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default AddPageModal
