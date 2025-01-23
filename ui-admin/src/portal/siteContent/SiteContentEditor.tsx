import React, {
  useMemo,
  useState
} from 'react'
import Api, {
  HtmlSection,
  NavbarItem
} from 'api/api'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faClockRotateLeft,
  faGlobe,
  faImage,
  faPalette,
  faPlus,
  faTrash
} from '@fortawesome/free-solid-svg-icons'
import HtmlPageEditView from './HtmlPageEditView'
import {
  ApiContextT,
  ApiProvider,
  HtmlPage,
  HtmlSectionView,
  LocalSiteContent,
  SiteContent,
  SiteFooter
} from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import SiteContentVersionSelector from './SiteContentVersionSelector'
import { Button } from 'components/forms/Button'
import AddPageModal from 'portal/siteContent/AddPageModal'
import CreatePreRegSurveyModal from '../CreatePreRegSurveyModal'
import { PortalEnvContext } from '../PortalRouter'
import ErrorBoundary from 'util/ErrorBoundary'
import {
  Modal,
  Tab,
  Tabs
} from 'react-bootstrap'
import DeleteNavItemModal from './DeleteNavItemModal'
import BrandingModal from './BrandingModal'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'
import { useConfig } from 'providers/ConfigProvider'

import _cloneDeep from 'lodash/cloneDeep'
import TranslationModal from './TranslationModal'
import useLanguageSelectorFromParam from '../languages/useLanguageSelector'
import { NavbarPreview } from 'portal/siteContent/NavbarPreview'
import LanguageTextOverridesEditor from 'portal/siteContent/LanguageTextOverridesEditor'

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

  const selectedLanguage = selectedLanguageOption?.value
  const [workingContent, setWorkingContent] = useState<SiteContent>(siteContent)
  const localContent = useMemo(
    () => workingContent.localizedSiteContents.find(lsc => lsc.language === selectedLanguage?.languageCode),
    [workingContent, selectedLanguage])

  const { portalEnv } = portalEnvContext
  const [activeTab, setActiveTab] = useState<string | null>('designer')
  const [selectedPagePath, setSelectedPagePath] = useState<string>()

  const isLandingPage = selectedPagePath === undefined || selectedPagePath === localContent?.landingPage.path

  const selectedPage = useMemo(
    () => isLandingPage ? localContent?.landingPage : localContent?.pages.find(p => p.path === selectedPagePath),
    [localContent, selectedPagePath, isLandingPage])

  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const [showAddPageModal, setShowAddPageModal] = useState(false)
  const [showBrandingModal, setShowBrandingModal] = useState(false)
  const [showDeletePageModal, setShowDeletePageModal] = useState(false)
  const [showAddPreRegModal, setShowAddPreRegModal] = useState(false)
  const [showUnsavedPreviewModal, setShowUnsavedPreviewModal] = useState(false)
  const [showTranslationModal, setShowTranslationModal] = useState(false)
  const [hasInvalidSection, setHasInvalidSection] = useState(false)
  const zoneConfig = useConfig()

  /** updates the global SiteContent object with the given LocalSiteContent */
  const updateLocalContent = (localContent: LocalSiteContent) => {
    const updatedLocalContents = [...workingContent.localizedSiteContents]
    const matchedIndex = workingContent.localizedSiteContents
      .findIndex(lsc => lsc.language === localContent.language)
    updatedLocalContents[matchedIndex] = localContent
    const newWorkingContent: SiteContent = {
      ...workingContent,
      localizedSiteContents: updatedLocalContents
    }

    setWorkingContent(newWorkingContent)
  }

  /** creates a new local site content for the current language based on the content for the default language */
  const addLocalContent = () => {
    const defaultContent = workingContent.localizedSiteContents
      .find(lsc => lsc.language === defaultLanguage.languageCode)
    if (!selectedLanguage || !defaultContent) {
      return
    }
    const newLocalContent: LocalSiteContent = {
      ..._cloneDeep(defaultContent),
      language: selectedLanguage!.languageCode
    }
    const updatedLocalContents = [...workingContent.localizedSiteContents, newLocalContent]
    const newWorkingContent: SiteContent = {
      ...workingContent,
      localizedSiteContents: updatedLocalContents
    }
    setWorkingContent(newWorkingContent)
  }

  const insertNewPage = (newPage: HtmlPage) => {
    if (!localContent) {
      return
    }
    const updatedLocalContent = {
      ...localContent,
      pages: [...localContent.pages, newPage]
    }
    updateLocalContent(updatedLocalContent)
    setSelectedPagePath(newPage.path)
  }

  const deletePage = (path: string) => {
    if (!localContent) {
      return
    }
    const updatedPages = [...localContent.pages]
    const matchedPageIndex = updatedPages.findIndex(page => page.path === path)
    if (matchedPageIndex === -1) {
      return
    }
    updatedPages.splice(matchedPageIndex, 1)
    const updatedLocalContent = {
      ...localContent,
      pages: updatedPages
    }

    updateLocalContent(updatedLocalContent)
    setSelectedPagePath(updatedLocalContent.landingPage.path)
  }

  /** updates the global SiteContent object with the given HtmlPage */
  const updatePage = (updatedPage: HtmlPage, isLandingPage: boolean) => {
    if (!localContent) {
      return
    }
    let updatedLocalContent
    if (isLandingPage) {
      updatedLocalContent = {
        ...localContent,
        landingPage: updatedPage
      }
    } else {
      const updatedPages = [...localContent.pages]

      const matchedPageIndex = updatedPages.findIndex(p => p.path === updatedPage.path)
      if (matchedPageIndex === -1) {
        return
      }

      updatedPages[matchedPageIndex] = updatedPage

      updatedLocalContent = {
        ...localContent,
        pages: updatedPages
      }
    }
    updateLocalContent(updatedLocalContent)
  }

  const updateFooter = (footer?: HtmlSection) => {
    if (!localContent) {
      return
    }
    const updatedLocalContent = {
      ...localContent,
      footerSection: footer
    }
    updateLocalContent(updatedLocalContent)
  }

  const updateNavbarItems = (items: NavbarItem[]) => {
    if (!localContent) {
      return
    }
    const updatedLocalContent = {
      ...localContent,
      navbarItems: items
    }
    updateLocalContent(updatedLocalContent)
  }

  const participantViewClick = () => {
    if (hasInvalidSection || (siteContent !== workingContent)) {
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

  const pageOpts: {
    label: string,
    value: string
  }[] = (localContent?.landingPage ? [localContent.landingPage] : [])
    .concat(localContent?.pages || [])
    .map(page => ({ label: page.title || 'Landing page', value: page.path }))


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
              <Button className="me-md-2" variant="primary"
                disabled={readOnly || hasInvalidSection || (siteContent === workingContent)}
                tooltipPlacement={'left'}
                tooltip={(() => {
                  if (siteContent === workingContent) {
                    return 'Site is unchanged. Make changes to save.'
                  }
                  if (hasInvalidSection) {
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
      <div className="px-2">
        <div className="d-flex flex-grow-1 mb-1">
          <div style={{ width: 250 }}>
            <Select
              options={pageOpts}
              value={pageOpts.find(opt => opt.value === selectedPagePath)}
              isDisabled={hasInvalidSection} aria-label={'Select a page'}
              onChange={e => {
                setSelectedPagePath(
                  localContent?.pages.find(p => p.path === e?.value)?.path || localContent!.landingPage.path
                )
              }}/>
          </div>
          <Button className="btn btn-secondary"
            tooltip={'Add a new page'}
            disabled={readOnly || !isEditable || hasInvalidSection}
            onClick={() => setShowAddPageModal(!showAddPageModal)}>
            <FontAwesomeIcon icon={faPlus}/> Add
          </Button>
          <Button className="btn btn-secondary"
            tooltip={!isLandingPage ? 'Delete this page' : 'You cannot delete the landing page'}
            disabled={readOnly || !isEditable || hasInvalidSection || isLandingPage}
            onClick={() => setShowDeletePageModal(!showAddPageModal)}>
            <FontAwesomeIcon icon={faTrash}/> Delete
          </Button>
          { languageOptions.length > 1 && <div className="ms-2" style={{ width: 200 }}>
            <Select options={languageOptions} value={selectedLanguageOption} inputId={selectLanguageInputId}
              isDisabled={hasInvalidSection}
              aria-label={'Change site content editor language'}
              onChange={languageOnChange}/>
          </div> }
          <div className="d-flex ms-auto">
            <Button variant="light" className='border' onClick={() => setShowBrandingModal(true)}>
              <FontAwesomeIcon icon={faPalette} className="fa-lg"/> Branding
            </Button>
            <Link to="../media" className="btn btn-light ms-2 border">
              <FontAwesomeIcon icon={faImage} className="fa-lg"/> Media
            </Link>
            <Button variant="light" onClick={() => setShowTranslationModal(true)} className="ms-2 border">
              <FontAwesomeIcon icon={faGlobe} className="fa-lg"/> Translations
            </Button>
          </div>
        </div>
      </div>
      { !localContent && <div className="d-flex flex-column flex-grow-1 mt-2">
        <div className="alert alert-warning" role="alert">
          No content has been configured for this language.
          <Button className="btn btn-secondary ms-3" onClick={addLocalContent}>
            Clone from default
          </Button>
        </div>
      </div>}
      {localContent && <div className="d-flex flex-column flex-grow-1 mt-2">
        {selectedPage &&
          <Tabs
            activeKey={activeTab ?? undefined}
            className="mb-1"
            mountOnEnter
            unmountOnExit
            onSelect={setActiveTab}
          >
            <Tab
              eventKey="designer"
              title={'Designer'}
              disabled={hasInvalidSection}
            >
              <ErrorBoundary>
                <div>
                  {selectedPage &&
                      <ApiProvider api={previewApi}>
                        <HtmlPageEditView
                          localSiteContent={localContent}
                          updateNavbarItems={updateNavbarItems}
                          htmlPage={selectedPage}
                          readOnly={readOnly}
                          portalEnvContext={portalEnvContext}
                          siteHasInvalidSection={hasInvalidSection}
                          setSiteHasInvalidSection={setHasInvalidSection}
                          footerSection={localContent.footerSection}
                          updateFooter={updateFooter}
                          updatePage={page => updatePage(page, isLandingPage)}
                          useJsonEditor={false}/>
                      </ApiProvider>}
                </div>
              </ErrorBoundary>
            </Tab>
            <Tab
              eventKey="json"
              title="JSON Editor"
              disabled={hasInvalidSection}
            >
              <ErrorBoundary>
                <div>
                  {selectedPage &&
                      <ApiProvider api={previewApi}>
                        <HtmlPageEditView
                          portalEnvContext={portalEnvContext}
                          localSiteContent={localContent}
                          updateNavbarItems={updateNavbarItems}
                          htmlPage={selectedPage}
                          readOnly={readOnly}
                          siteHasInvalidSection={hasInvalidSection} setSiteHasInvalidSection={setHasInvalidSection}
                          footerSection={localContent.footerSection} updateFooter={updateFooter}
                          updatePage={page => updatePage(page, isLandingPage)}/>
                      </ApiProvider>}
                </div>
              </ErrorBoundary>
            </Tab>
            <Tab
              eventKey={'systemTextOverride'}
              title={'Text Overrides'}
              disabled={hasInvalidSection}
            >
              <ErrorBoundary>
                <ApiProvider api={previewApi}>
                  <LanguageTextOverridesEditor
                    portalEnvContext={portalEnvContext}
                    localContent={localContent}
                    selectedLanguage={selectedLanguage?.languageCode || ''}
                    onChange={overrides => {
                      const updatedLocalContent = {
                        ...localContent,
                        languageTextOverrides: overrides
                      }
                      updateLocalContent(updatedLocalContent)
                    }}
                  />
                </ApiProvider>
              </ErrorBoundary>
            </Tab>
            <Tab
              eventKey="preview"
              title="Preview"
              disabled={hasInvalidSection}
            >
              <ErrorBoundary>
                <ApiProvider api={previewApi}>
                  <NavbarPreview
                    portal={portalEnvContext.portal}
                    portalEnv={portalEnv}
                    localContent={localContent}
                  />
                  {selectedPage.sections.map((section: HtmlSection) =>
                    <HtmlSectionView section={section} key={section.id}/>)
                  }
                  <SiteFooter footerSection={localContent.footerSection}/>
                </ApiProvider>
              </ErrorBoundary>
            </Tab>
          </Tabs> }
        { showVersionSelector &&
          <SiteContentVersionSelector portalShortcode={portalEnvContext.portal.shortcode}
            stableId={siteContent.stableId} current={siteContent}
            loadSiteContent={loadSiteContent} portalEnv={portalEnv}
            switchToVersion={switchToVersion}
            onDismiss={() => setShowVersionSelector(false)}/>
        }
        { showAddPageModal &&
            <AddPageModal
              insertNewPage={insertNewPage}
              onDismiss={() => setShowAddPageModal(false)}
              portalShortcode={portalEnvContext.portal.shortcode}
              portalEnv={portalEnv}
            />
        }
        {(showDeletePageModal && selectedPage) &&
            <DeleteNavItemModal
              page={selectedPage}
              deletePage={() => deletePage(selectedPage.path)}
              onDismiss={() => setShowDeletePageModal(false)}/>
        }
        { showAddPreRegModal &&
          <CreatePreRegSurveyModal portalEnvContext={portalEnvContext} onDismiss={() => setShowAddPreRegModal(false)}/>
        }
        { showBrandingModal &&
          <BrandingModal onDismiss={() => setShowBrandingModal(false)} localContent={localContent}
            updateLocalContent={updateLocalContent} portalShortcode={portalEnvContext.portal.shortcode}/>
        }
        { showTranslationModal &&
          <TranslationModal onDismiss={() => setShowTranslationModal(false)}
            siteContent={workingContent}
            setSiteContent={setWorkingContent} />
        }
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
    </div>
  </div>
}

export default SiteContentEditor

