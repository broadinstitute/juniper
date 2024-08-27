import React, { useState } from 'react'
import Api from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClockRotateLeft } from '@fortawesome/free-solid-svg-icons'
import {
  ApiContextT,
  SiteContent
} from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { Button } from 'components/forms/Button'
import { PortalEnvContext } from '../PortalRouter'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'
import { useConfig } from 'providers/ConfigProvider'
import useLanguageSelectorFromParam from '../languages/useLanguageSelector'
import Select from 'react-select'
import Modal from 'react-bootstrap/Modal'
import { NavbarEditor } from 'portal/siteContent/NavbarEditor'

type InitializedSiteContentViewProps = {
  siteContent: SiteContent
  previewApi: ApiContextT
  loadSiteContent: (stableId: string, version: number, language?: string) => void
  createNewVersion: (content: SiteContent) => void
  switchToVersion: (id: string, stableId: string, version: number) => void
  portalEnvContext: PortalEnvContext
  readOnly: boolean
}

/** shows a site content in editable form with a live preview.  Defaults to english-only for now */
const SiteContentEditor = (props: InitializedSiteContentViewProps) => {
  const {
    siteContent, previewApi, portalEnvContext, loadSiteContent, switchToVersion, createNewVersion, readOnly
  } = props

  const {
    defaultLanguage, languageOnChange, selectedLanguageOption,
    selectLanguageInputId, languageOptions
  } = useLanguageSelectorFromParam()
  const [workingContent, setWorkingContent] = useState<SiteContent>(siteContent)

  const { portalEnv } = portalEnvContext

  const [isEditingNavbar, setIsEditingNavbar] = useState(false)
  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const [showUnsavedPreviewModal, setShowUnsavedPreviewModal] = useState(false)
  const [hasInvalidNavbarItem, setHasInvalidNavbarItem] = useState(false)
  const [hasInvalidSection, setHasInvalidSection] = useState(false)

  const isInvalid = hasInvalidNavbarItem || hasInvalidSection

  const zoneConfig = useConfig()


  const participantViewClick = () => {
    if (isInvalid || (siteContent !== workingContent)) {
      setShowUnsavedPreviewModal(true)
    } else {
      showParticipantView()
    }
  }

  const showParticipantView = () => {
    const url = Api.getParticipantLink(portalEnv.portalEnvironmentConfig, zoneConfig.participantUiHostname,
      portalEnvContext.portal.shortcode, portalEnv.environmentName)
    window.open(url, '_blank')
  }

  const isEditable = !readOnly && portalEnv.environmentName === 'sandbox'

  return <div className="d-flex bg-white pb-5">
    <div className="d-flex flex-column flex-grow-1 mx-1 mb-1">
      <div className="d-flex p-2">
        <div className="d-flex flex-grow-1 align-items-center">
          <h5>Website Content
            <span className="fs-6 text-muted fst-italic me-2 ms-2">
            (v{siteContent.version})
            </span>
            {isEditable && <button className="btn btn-secondary"
              onClick={() => setShowVersionSelector(!showVersionSelector)}>
              <FontAwesomeIcon icon={faClockRotateLeft}/> History
            </button> }
            <Button variant="secondary" className="ms-5" onClick={participantViewClick}>
              Participant view <FontAwesomeIcon icon={faExternalLink}/>
            </Button>
          </h5>
        </div>
        <div className="d-flex flex-grow-1 justify-content-end align-items-center">
          {
            isEditable && <>
              { languageOptions.length > 1 && <div className="ms-2" style={{ width: 200 }}>
                <Select options={languageOptions} value={selectedLanguageOption} inputId={selectLanguageInputId}
                  isDisabled={isInvalid} aria-label={'Select a language'}
                  onChange={languageOnChange}/>
              </div> }
              <Button className="me-md-2" variant="primary"
                disabled={readOnly || isInvalid || (siteContent === workingContent)}
                tooltipPlacement={'left'}
                tooltip={(() => {
                  if (siteContent === workingContent) {
                    return 'Site is unchanged. Make changes to save.'
                  }
                  if (isInvalid) {
                    return 'Site is invalid. Correct to save.'
                  }
                  return 'Save changes'
                })()}
                onClick={() => createNewVersion(workingContent)}>
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

      {isEditingNavbar ? <NavbarEditor
        createNewVersion={createNewVersion}
        switchToVersion={switchToVersion}
        portalEnvContext={portalEnvContext}
        siteContent={workingContent}
      /> : <SiteContentEditor
        siteContent={siteContent}
        previewApi={previewApi}
        loadSiteContent={loadSiteContent}
        createNewVersion={createNewVersion}
        switchToVersion={switchToVersion}
        portalEnvContext={portalEnvContext}
        readOnly={readOnly}

      />}

    </div>

    { showUnsavedPreviewModal &&
        <Modal show={true} onHide={() => setShowUnsavedPreviewModal(false)}>
          <Modal.Body>
                Please note that your unsaved changes will not appear in the participant view until you save them.
          </Modal.Body>
          <Modal.Footer>
            <button className="btn btn-primary" onClick={() => {
              showParticipantView()
              setShowUnsavedPreviewModal(false)
            }}>
                    Launch participant view in new tab
            </button>
            <button className="btn btn-secondary" onClick={() => setShowUnsavedPreviewModal(false)}>Cancel</button>
          </Modal.Footer>
        </Modal>
    }
  </div>
}

export default SiteContentEditor

