import React, { useState } from 'react'
import { HtmlSection, NavbarItemInternal, PortalEnvironment } from 'api/api'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faClockRotateLeft, faImage, faPlus } from '@fortawesome/free-solid-svg-icons'
import HtmlPageEditView from './HtmlPageEditView'
import { HtmlPage, LocalSiteContent, ApiProvider, SiteContent, ApiContextT, HtmlSectionView, SiteFooter } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import SiteContentVersionSelector from './SiteContentVersionSelector'
import { Button } from 'components/forms/Button'
import AddPageModal from './AddPageModal'
import ErrorBoundary from 'util/ErrorBoundary'
import { Tab, Tabs } from 'react-bootstrap'

type NavbarOption = {label: string, value: string}
const landingPageOption = { label: 'Landing page', value: 'Landing page' }

type InitializedSiteContentViewProps = {
  siteContent: SiteContent
  previewApi: ApiContextT
  loadSiteContent: (stableId: string, version: number) => void
  createNewVersion: (content: SiteContent) => void
  switchToVersion: (id: string, stableId: string, version: number) => void
  portalShortcode: string
  portalEnv: PortalEnvironment
  readOnly: boolean
}

/** shows a site content in editable form with a live preview.  Defaults to english-only for now */
const SiteContentEditor = (props: InitializedSiteContentViewProps) => {
  const {
    siteContent, previewApi, portalShortcode,
    portalEnv, loadSiteContent, switchToVersion, createNewVersion, readOnly
  } = props
  const selectedLanguage = 'en'
  const initialContent = siteContent
  const [activeTab, setActiveTab] = useState<string | null>('designer')
  const [selectedNavOpt, setSelectedNavOpt] = useState<NavbarOption>(landingPageOption)
  const [workingContent, setWorkingContent] = useState<SiteContent>(initialContent)
  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const [showAddPageModal, setShowAddPageModal] = useState(false)
  const localContent = workingContent.localizedSiteContents.find(lsc => lsc.language === selectedLanguage)
  const [siteInvalid, setSiteInvalid] = useState(false)
  if (!localContent) {
    return <div>no content for language {selectedLanguage}</div>
  }
  const navBarInternalItems = localContent.navbarItems
    .filter((navItem): navItem is NavbarItemInternal => navItem.itemType === 'INTERNAL')

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

  const insertNewPage = (page: HtmlPage) => {
    if (!localContent) {
      return
    }
    const newNavBarItem: NavbarItemInternal = {
      itemType: 'INTERNAL',
      itemOrder: localContent.navbarItems.length,
      text: page.title,
      htmlPage: page
    }
    const updatedLocalContent = {
      ...localContent,
      navbarItems: [...localContent.navbarItems, newNavBarItem]
    }
    updateLocalContent(updatedLocalContent)
    setSelectedNavOpt({ label: newNavBarItem.text, value: newNavBarItem.text || 'Landing page' })
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
      const matchedNavItem = navBarInternalItems.find(navItem => navItem.text === navItemText)
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

  const updateFooter = (footer: HtmlSection | undefined) => {
    if (!localContent) {
      return
    }
    const updatedLocalContent = {
      ...localContent,
      footerSection: footer
    }
    updateLocalContent(updatedLocalContent)
  }

  const isEditable = !readOnly && portalEnv.environmentName === 'sandbox'

  const currentNavBarItem = selectedNavOpt.value ? navBarInternalItems
    .find(navItem => navItem.text === selectedNavOpt.value) : null
  const pageToRender = currentNavBarItem ? currentNavBarItem.htmlPage : localContent.landingPage

  const pageOpts: {label: string, value: string}[] = navBarInternalItems
    .map(navItem => ({ label: navItem.text, value: navItem.text }))
  pageOpts.unshift(landingPageOption)

  const hasFooter = !!localContent.footerSection

  return <div className="d-flex bg-white">
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
            <Link to="../images" className="btn btn-secondary">
              <FontAwesomeIcon icon={faImage} className="fa-lg"/> Manage images
            </Link>
          </h5>
        </div>
        {
          isEditable && <div className="d-flex flex-grow-1">
            <Button className="ms-auto me-md-2" variant="primary"
              disabled={readOnly || siteInvalid || (initialContent === workingContent)}
              tooltipPlacement={'left'}
              tooltip={(() => {
                if (initialContent === workingContent) {
                  return 'Site is unchanged. Make changes to save.'
                }
                if (siteInvalid) {
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
            <Select options={pageOpts} value={selectedNavOpt} isDisabled={siteInvalid} aria-label={'Select a page'}
              onChange={e => {
                setSelectedNavOpt(e ?? landingPageOption)
              }}/>
          </div>
          <Button className="btn btn-secondary"
            tooltip={'Add a new page'}
            disabled={readOnly || !isEditable || siteInvalid}
            onClick={() => setShowAddPageModal(!showAddPageModal)}>
            <FontAwesomeIcon icon={faPlus}/> Add page
          </Button>
        </div>

      </div>
      <div className="d-flex flex-column flex-grow-1 mt-2">
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
            disabled={siteInvalid}
          >
            <ErrorBoundary>
              <div>
                {pageToRender &&
                    <ApiProvider api={previewApi}>
                      <HtmlPageEditView htmlPage={pageToRender} readOnly={readOnly}
                        siteInvalid={siteInvalid} setSiteInvalid={setSiteInvalid}
                        footerSection={localContent.footerSection} updateFooter={updateFooter}
                        updatePage={page => updatePage(page, currentNavBarItem?.text)}/>
                    </ApiProvider>}
              </div>
            </ErrorBoundary>
          </Tab>
          <Tab
            eventKey="preview"
            title="Preview"
            disabled={siteInvalid}
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
        </Tabs>
      </div>
    </div>
    { showVersionSelector &&
        <SiteContentVersionSelector portalShortcode={portalShortcode} stableId={siteContent.stableId}
          current={siteContent} loadSiteContent={loadSiteContent} portalEnv={portalEnv}
          switchToVersion={switchToVersion}
          onDismiss={() => setShowVersionSelector(false)}/>
    }
    { showAddPageModal &&
        <AddPageModal portalEnv={portalEnv} portalShortcode={portalShortcode} insertNewPage={insertNewPage}
          show={showAddPageModal} setShow={setShowAddPageModal}/>
    }
  </div>
}

export default SiteContentEditor

