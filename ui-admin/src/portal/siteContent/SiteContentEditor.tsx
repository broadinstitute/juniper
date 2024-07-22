import React, { useState } from 'react'
import Api, { HtmlSection, NavbarItem, NavbarItemExternal, NavbarItemInternal } from 'api/api'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faClockRotateLeft, faGlobe,
  faImage,
  faPalette, faPencil,
  faPlus,
  faTrash
} from '@fortawesome/free-solid-svg-icons'
import HtmlPageEditView from './HtmlPageEditView'
import {
  HtmlPage, LocalSiteContent, ApiProvider, SiteContent,
  ApiContextT, HtmlSectionView, SiteFooter
} from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import SiteContentVersionSelector from './SiteContentVersionSelector'
import { Button } from 'components/forms/Button'
import AddNavbarItemModal, { NavItemProps } from './AddNavbarItemModal'
import CreatePreRegSurveyModal from '../CreatePreRegSurveyModal'
import { PortalEnvContext } from '../PortalRouter'
import ErrorBoundary from 'util/ErrorBoundary'
import { Tab, Tabs } from 'react-bootstrap'
import DeleteNavItemModal from './DeleteNavItemModal'
import BrandingModal from './BrandingModal'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'
import { useConfig } from 'providers/ConfigProvider'
import Modal from 'react-bootstrap/Modal'

import _cloneDeep from 'lodash/cloneDeep'
import TranslationModal from './TranslationModal'
import useLanguageSelectorFromParam from '../languages/useLanguageSelector'
import UpdateNavItemModal from './UpdateNavItemModal'

type NavbarOption = {label: string, value: string}
const landingPageOption = { label: 'Landing page', value: 'Landing page' }

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
  const { portalEnv } = portalEnvContext
  const [activeTab, setActiveTab] = useState<string | null>('designer')
  const [selectedNavOpt, setSelectedNavOpt] = useState<NavbarOption>(landingPageOption)
  const [workingContent, setWorkingContent] = useState<SiteContent>(siteContent)
  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const [showAddPageModal, setShowAddPageModal] = useState(false)
  const [showBrandingModal, setShowBrandingModal] = useState(false)
  const [showDeletePageModal, setShowDeletePageModal] = useState(false)
  const [showUpdatePageModal, setShowUpdatePageModal] = useState(false)
  const [showAddPreRegModal, setShowAddPreRegModal] = useState(false)
  const [showUnsavedPreviewModal, setShowUnsavedPreviewModal] = useState(false)
  const [showTranslationModal, setShowTranslationModal] = useState(false)
  const [hasInvalidSection, setHasInvalidSection] = useState(false)
  const zoneConfig = useConfig()
  const {
    defaultLanguage, languageOnChange, selectedLanguageOption,
    selectLanguageInputId, languageOptions
  } = useLanguageSelectorFromParam()
  const selectedLanguage = selectedLanguageOption?.value
  const localContent = workingContent.localizedSiteContents.find(lsc => lsc.language === selectedLanguage?.languageCode)

  const navBarItems = localContent?.navbarItems ?? []

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

  const insertNewNavItem = (itemProps: NavItemProps) => {
    if (!localContent) {
      return
    }
    let newNavBarItem: NavbarItemInternal | NavbarItemExternal
    if (itemProps.itemType === 'INTERNAL') {
      newNavBarItem = {
        itemType: 'INTERNAL',
        itemOrder: localContent.navbarItems.length,
        text: itemProps.text,
        htmlPage: {
          path: itemProps.href,
          sections: [],
          title: itemProps.text
        }
      }
    } else {
      newNavBarItem = {
        itemType: 'EXTERNAL',
        itemOrder: localContent.navbarItems.length,
        text: itemProps.text,
        href: itemProps.href
      }
    }
    const updatedLocalContent = {
      ...localContent,
      navbarItems: [...localContent.navbarItems, newNavBarItem]
    }
    updateLocalContent(updatedLocalContent)
    setSelectedNavOpt({ label: newNavBarItem.text, value: newNavBarItem.text || 'Landing page' })
  }

  const deleteNavItem = (navItemText: string) => {
    if (!localContent) {
      return
    }
    const updatedNavBarItems = [...localContent.navbarItems]
    const matchedNavItemIndex = updatedNavBarItems.findIndex(navItem => navItem.text === navItemText)
    if (matchedNavItemIndex === -1) {
      return
    }
    updatedNavBarItems.splice(matchedNavItemIndex, 1)
    const updatedLocalContent = {
      ...localContent,
      navbarItems: updatedNavBarItems
    }

    updateLocalContent(updatedLocalContent)
    setSelectedNavOpt(landingPageOption)
  }

  const updateNavItem = (navItem: NavbarItem) => {
    if (!localContent) {
      return
    }
    const updatedNavBarItems = [...localContent.navbarItems]
    const matchedNavItem = updatedNavBarItems.find(item => item.itemOrder === navItem.itemOrder)
    if (!matchedNavItem) {
      return
    }
    updatedNavBarItems[matchedNavItem.itemOrder] = navItem
    const updatedLocalContent = {
      ...localContent,
      navbarItems: updatedNavBarItems
    }
    setSelectedNavOpt({ label: navItem.text, value: navItem.text })
    updateLocalContent(updatedLocalContent)
  }

  /** updates the global SiteContent object with the given HtmlPage, which may be associated with a navItem */
  const updatePage = (page: HtmlPage, navItemText?: string) => {
    if (!localContent) {
      return
    }
    let updatedLocalContent
    if (!navItemText) {
      updatedLocalContent = {
        ...localContent,
        landingPage: page
      }
    } else {
      const updatedNavBarItems = [...localContent.navbarItems]
      const matchedNavItem = navBarItems.find(navItem => navItem.text === navItemText) as NavbarItemInternal
      if (!matchedNavItem) {
        return
      }
      updatedNavBarItems[matchedNavItem.itemOrder] = {
        ...matchedNavItem,
        htmlPage: page
      }
      updatedLocalContent = {
        ...localContent,
        navbarItems: updatedNavBarItems
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

  const currentNavBarItem = selectedNavOpt.value ? navBarItems
    .find(navItem => navItem.text === selectedNavOpt.value) : null
  const pageToRender = currentNavBarItem ?
    (currentNavBarItem as NavbarItemInternal).htmlPage : localContent?.landingPage

  const pageOpts: {label: string, value: string}[] = navBarItems
    .map(navItem => ({ label: navItem.text, value: navItem.text }))
  pageOpts.unshift(landingPageOption)

  const isLandingPage = selectedNavOpt === landingPageOption

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
            <Select options={pageOpts} value={selectedNavOpt}
              isDisabled={hasInvalidSection} aria-label={'Select a page'}
              onChange={e => {
                setSelectedNavOpt(e ?? landingPageOption)
              }}/>
          </div>
          <Button className="btn btn-secondary"
            tooltip={'Add a new page'}
            disabled={readOnly || !isEditable || hasInvalidSection}
            onClick={() => setShowAddPageModal(!showAddPageModal)}>
            <FontAwesomeIcon icon={faPlus}/> Add
          </Button>
          <Button className="btn btn-secondary"
            tooltip={!isLandingPage ? 'Edit page configuration' : 'You cannot edit configuration for the landing page'}
            disabled={readOnly || !isEditable || hasInvalidSection || isLandingPage}
            onClick={() => setShowUpdatePageModal(!showUpdatePageModal)}>
            <FontAwesomeIcon icon={faPencil}/> Edit config
          </Button>
          <Button className="btn btn-secondary"
            tooltip={!isLandingPage ? 'Delete this page' : 'You cannot delete the landing page'}
            disabled={readOnly || !isEditable || hasInvalidSection || isLandingPage}
            onClick={() => setShowDeletePageModal(!showAddPageModal)}>
            <FontAwesomeIcon icon={faTrash}/> Delete
          </Button>
          { languageOptions.length > 1 && <div className="ms-2" style={{ width: 200 }}>
            <Select options={languageOptions} value={selectedLanguageOption} inputId={selectLanguageInputId}
              isDisabled={hasInvalidSection} aria-label={'Select a language'}
              onChange={languageOnChange}/>
          </div> }
          <div className="d-flex ms-auto">
            <Button variant="light" onClick={() => setShowBrandingModal(true)}>
              Branding <FontAwesomeIcon icon={faPalette} className="fa-lg"/>
            </Button>
            <Link to="../media" className="btn btn-light ms-2">
              Media <FontAwesomeIcon icon={faImage} className="fa-lg"/>
            </Link>
            <Button variant="light" onClick={() => setShowTranslationModal(true)} className="ms-2">
              Translations <FontAwesomeIcon icon={faGlobe} className="fa-lg"/>
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
        { pageToRender &&
          <Tabs
            activeKey={activeTab ?? undefined}
            className="mb-1"
            mountOnEnter
            unmountOnExit
            onSelect={setActiveTab}
          >
            <Tab
              eventKey="designer"
              title={<>Designer<span className='badge bg-primary fw-light ms-2'>BETA</span></>}
              disabled={hasInvalidSection}
            >
              <ErrorBoundary>
                <div>
                  {pageToRender &&
                      <ApiProvider api={previewApi}>
                        <HtmlPageEditView htmlPage={pageToRender} readOnly={readOnly}
                          portalEnvContext={portalEnvContext}
                          siteHasInvalidSection={hasInvalidSection} setSiteHasInvalidSection={setHasInvalidSection}
                          footerSection={localContent.footerSection} updateFooter={updateFooter}
                          updatePage={page => updatePage(page, currentNavBarItem?.text)} useJsonEditor={false}/>
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
                  {pageToRender &&
                      <ApiProvider api={previewApi}>
                        <HtmlPageEditView portalEnvContext={portalEnvContext} htmlPage={pageToRender}
                          readOnly={readOnly}
                          siteHasInvalidSection={hasInvalidSection} setSiteHasInvalidSection={setHasInvalidSection}
                          footerSection={localContent.footerSection} updateFooter={updateFooter}
                          updatePage={page => updatePage(page, currentNavBarItem?.text)}/>
                      </ApiProvider>}
                </div>
              </ErrorBoundary>
            </Tab>
            <Tab
              eventKey="preview"
              title="Preview"
              disabled={hasInvalidSection}
            >
              <ErrorBoundary>
                <ApiProvider api={previewApi}>
                  { pageToRender.sections.map((section: HtmlSection) =>
                    <HtmlSectionView section={section} key={section.id}/>)
                  }
                  <SiteFooter footerSection={localContent.footerSection}/>
                </ApiProvider>
              </ErrorBoundary>
            </Tab>
          </Tabs> }
        { currentNavBarItem?.itemType === 'EXTERNAL' && <div className="mt-2">
          <form className="container">
            <label htmlFor="externalHref">External link</label>
            <input type="text" id="externalHref" className="form-control"
              value={(currentNavBarItem as NavbarItemExternal).href}
              onChange={e => {
                const updatedNavItem = { ...currentNavBarItem, href: e.target.value }
                const updatedNavBarItems = [...localContent.navbarItems]
                updatedNavBarItems[updatedNavItem.itemOrder] = updatedNavItem
                const updatedLocalContent = {
                  ...localContent,
                  navbarItems: updatedNavBarItems
                }
                updateLocalContent(updatedLocalContent)
              }}/>
          </form>
        </div>
        }
        { showVersionSelector &&
          <SiteContentVersionSelector portalShortcode={portalEnvContext.portal.shortcode}
            stableId={siteContent.stableId} current={siteContent}
            loadSiteContent={loadSiteContent} portalEnv={portalEnv}
            switchToVersion={switchToVersion}
            onDismiss={() => setShowVersionSelector(false)}/>
        }
        { showAddPageModal &&
          <AddNavbarItemModal portalEnv={portalEnv} portalShortcode={portalEnvContext.portal.shortcode}
            insertNewNavItem={insertNewNavItem}
            onDismiss={() => setShowAddPageModal(false)}/>
        }
        { (showUpdatePageModal && currentNavBarItem) &&
          <UpdateNavItemModal navItem={currentNavBarItem} updateNavItem={updateNavItem}
            onDismiss={() => setShowUpdatePageModal(false)}/>
        }
        { (showDeletePageModal && currentNavBarItem) &&
          <DeleteNavItemModal navItem={currentNavBarItem} deleteNavItem={deleteNavItem}
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

