import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { SiteContent } from '@juniper/ui-core'
import { languageExtractToCSV, languageImportFromCSV } from './siteContentLanguageUtils'
import { saveBlobAsDownload } from 'util/downloadUtils'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import { Button } from '../../components/forms/Button'
import { useFileUploadButton } from '../../util/uploadUtils'

/** renders a modal for selecting translation functions */
const TranslationModal = ({ onDismiss, siteContent, setSiteContent }: {
  siteContent: SiteContent, setSiteContent: (siteContent: SiteContent) => void, onDismiss: () => void
}) => {
  const extractTexts = () => {
    const csvString = languageExtractToCSV(siteContent)
    const blob = new Blob([csvString], { type: 'text/plain' })
    saveBlobAsDownload(blob, 'site-translations.csv')
  }
  const { FileChooser } = useFileUploadButton(file => {
    const reader = new FileReader()
    reader.onload = () => {
      const updatedContent = languageImportFromCSV(siteContent, reader.result as string)
      setSiteContent(updatedContent)
    }
    reader.readAsText(file)
  }, 'Import translation CSV')

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
      <div>
        { FileChooser }
      </div>
    </Modal.Body>
    <Modal.Footer>
      <button className="btn btn-secondary" onClick={onDismiss}>Done</button>
    </Modal.Footer>
  </Modal>
}

export default TranslationModal
