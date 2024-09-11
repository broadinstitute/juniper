import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import {
  HtmlPage,
  HtmlSection,
  PortalEnvironment,
  SectionType
} from '@juniper/ui-core'
import { sectionTemplates } from 'portal/siteContent/sectionTemplates'
import Api from 'api/api'
import { useConfig } from 'providers/ConfigProvider'

const createDefaultSection = (title: string): HtmlSection => {
  return {
    id: '',
    sectionType: 'HERO_WITH_IMAGE' as SectionType,
    sectionConfig: JSON.stringify({
      ...sectionTemplates['HERO_CENTERED'],
      title,
      blurb: 'Add content here'
    })
  }
}

const EMPTY_PAGE: HtmlPage = {
  path: '',
  title: '',
  sections: []
}


/** renders a modal that adds a new page to the site */
const AddPageModal = ({ portalEnv, portalShortcode, insertNewPage, onDismiss }: {
  portalEnv: PortalEnvironment, portalShortcode: string,
  insertNewPage: (item: HtmlPage) => void,
  onDismiss: () => void
}) => {
  const [page, setPage] = useState(EMPTY_PAGE)

  const addPage = async () => {
    insertNewPage({
      ...page,
      sections: [createDefaultSection(page.title)]
    })
    onDismiss()
  }

  const isItemValid = (page: HtmlPage) => {
    return page.path.length > 0
      && page.title.length > 0
      && /^[a-zA-Z0-9]+$/.test(page.path) // is alphanumeric
  }

  const clearFields = () => {
    setPage(EMPTY_PAGE)
  }

  const zoneConfig = useConfig()

  const portalUrl = Api.getParticipantLink(
    portalEnv.portalEnvironmentConfig, zoneConfig.participantUiHostname,
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
        <input
          type="text"
          size={50}
          className="form-control mb-3"
          id="inputPageTitle"
          data-testid="page-title-input"
          value={page.title}
          onChange={event => {
            setPage({ ...page, title: event.target.value })
          }}/>

        <label htmlFor="inputPagePath">Page Path</label>
        <div className="input-group">
          <div className="input-group-prepend">
            <span className="input-group-text" style={{ borderTopRightRadius: 0, borderBottomRightRadius: 0 }}
              id="pathPrefix">{portalUrl}/</span>
          </div>
          <input type="text"
            className="form-control"
            id="inputPagePath"
            value={page.path} aria-describedby="pathPrefix"
            data-testid="page-path-input"
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
