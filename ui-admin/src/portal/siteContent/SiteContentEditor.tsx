import React, { useState } from 'react'
import Api, { HtmlSection, NavbarItemExternal, NavbarItemInternal } from 'api/api'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClipboard, faClockRotateLeft, faImage, faPalette, faPlus, faTrash } from '@fortawesome/free-solid-svg-icons'
import HtmlPageEditView from './HtmlPageEditView'
import {
  HtmlPage, LocalSiteContent, ApiProvider, SiteContent,
  ApiContextT, HtmlSectionView, SiteFooter, PortalEnvironmentLanguage
} from '@juniper/ui-core'
import { Link, useSearchParams } from 'react-router-dom'
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
import { useNonNullReactSingleSelect } from 'util/react-select-utils'
import { usePortalLanguage } from '../languages/usePortalLanguage'


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
  const [showAddPreRegModal, setShowAddPreRegModal] = useState(false)
  const [showUnsavedPreviewModal, setShowUnsavedPreviewModal] = useState(false)
  const [hasInvalidSection, setHasInvalidSection] = useState(false)

  const zoneConfig = useConfig()
  const [searchParams, setSearchParams] = useSearchParams()
  const { defaultLanguage } = usePortalLanguage()
  const selectedLanguageCode = searchParams.get('lang') ?? defaultLanguage.languageCode
  const selectedLanguage = portalEnvContext.portalEnv.supportedLanguages.find(portalLang =>
    portalLang.languageCode === selectedLanguageCode)
  const localContent = workingContent.localizedSiteContents.find(lsc => lsc.language === selectedLanguage?.languageCode)

  const navBarItems = localContent?.navbarItems ?? []
  const setSelectedLanguage = (language: PortalEnvironmentLanguage | undefined) => {
    if (!language) {
      return
    }
    setSearchParams({ ...searchParams, lang: language.languageCode })
  }

  const {
    onChange: languageOnChange, options: languageOptions,
    selectedOption: selectedLanguageOption, selectInputId: selectLanguageInputId
  } =
    useNonNullReactSingleSelect(
        portalEnvContext.portalEnv.supportedLanguages as PortalEnvironmentLanguage[],
        language => ({ label: language?.languageName, value: language }),
        setSelectedLanguage,
        selectedLanguage
    )

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
      <div className="d-flex p-2 align-items-center">
        <div className="d-flex flex-grow-1">
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
        {
          isEditable && <div className="d-flex flex-grow-1">
            <Button className="ms-auto me-md-2" variant="primary"
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
          </div>
        }
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
          { portalEnvContext.portalEnv.supportedLanguages.length > 1 && <div className="ms-2" style={{ width: 200 }}>
            <Select options={languageOptions} value={selectedLanguageOption} inputId={selectLanguageInputId}
              isDisabled={hasInvalidSection} aria-label={'Select a language'}
              onChange={e => {
                if (e?.value) {
                  languageOnChange(e)
                }
              }}/>
          </div> }
          <Button className="btn btn-secondary"
            tooltip={'Add a new page'}
            disabled={readOnly || !isEditable || hasInvalidSection}
            onClick={() => setShowAddPageModal(!showAddPageModal)}>
            <FontAwesomeIcon icon={faPlus}/> Add navbar item
          </Button>
          <Button className="btn btn-secondary"
            tooltip={!isLandingPage ? 'Delete this page' : 'You cannot delete the landing page'}
            disabled={readOnly || !isEditable || hasInvalidSection || isLandingPage}
            onClick={() => setShowDeletePageModal(!showAddPageModal)}>
            <FontAwesomeIcon icon={faTrash}/> Delete navbar item
          </Button>
          <Button variant="light"  className="ms-auto  border m-1" tooltip={''}
            onClick={() => setShowBrandingModal(true)}
          >
            <FontAwesomeIcon icon={faPalette} className="fa-lg"/> Branding
          </Button>
          <Link to="../media" className="btn btn-light border m-1">
            <FontAwesomeIcon icon={faImage} className="fa-lg"/> Manage media
          </Link>
          { portalEnv.preRegSurveyId &&
            <Link to={'../forms/preReg'} className="btn btn-light border m-1">
              <FontAwesomeIcon icon={faClipboard} className="fa-lg"/> Pre-registration
            </Link> }
          { !portalEnv.preRegSurveyId &&
            <Button variant="light"  className="border m-1" tooltip={'Add a pre-registration survey that' +
                ' users must complete before being able to sign up for the portal.'}
            onClick={() => setShowAddPreRegModal(true)}
            >
              <FontAwesomeIcon icon={faClipboard} className="fa-lg"/> Pre-registration
            </Button> }
        </div>

      </div>
      { !localContent && <div className="d-flex flex-column flex-grow-1 mt-2">
        <div className="alert alert-warning" role="alert">
          No content has been configured for this language.
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
              title="Designer"
              disabled={hasInvalidSection}
            >
              <ErrorBoundary>
                <div>
                  {pageToRender &&
                      <ApiProvider api={previewApi}>
                        <HtmlPageEditView htmlPage={pageToRender} readOnly={readOnly}
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

