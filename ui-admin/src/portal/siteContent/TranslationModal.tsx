import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { SiteContent } from '@juniper/ui-core'
import { extractAllTexts, languageExtractToCSV } from './siteContentLanguageUtils'
import { saveBlobAsDownload } from 'util/downloadUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import { Button } from '../../components/forms/Button'

/** renders a modal for selecting translation functions */
const TranslationModal = ({ onDismiss, siteContent }: {
  siteContent: SiteContent, onDismiss: () => void
}) => {
  const extractTexts = () => {
    const extracts = extractAllTexts(siteContent)
    const csvString = languageExtractToCSV(extracts)
    const blob = new Blob([csvString], { type: 'text/plain' })
    saveBlobAsDownload(blob, 'site-translations.csv')
  }
  return <Modal show={true}
    onHide={() => {
      onDismiss()
    }}>
    <Modal.Header closeButton>
      <Modal.Title>Translation</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="mb-3">
        <Button variant="secondary" onClick={extractTexts}>Download texts <FontAwesomeIcon icon={faDownload}/>
        </Button>
      </div>
    </Modal.Body>
    <Modal.Footer>
      <button className="btn btn-secondary" onClick={onDismiss}>Done</button>
    </Modal.Footer>
  </Modal>
}

export default TranslationModal
