import React, { useState } from 'react'
import { NavbarItemInternal, PortalEnvironment } from 'api/api'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import HtmlPageEditView from './HtmlPageEditView'
import { HtmlPage, LocalSiteContent, ApiProvider, SiteContent, ApiContextT } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import SiteContentVersionSelector from './SiteContentVersionSelector'
import { Button } from '../../components/forms/Button'

type NavbarOption = {label: string, value: string | null}
const landingPageOption = { label: 'Landing page', value: null }

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
  const [selectedNavOpt, setSelectedNavOpt] = useState<NavbarOption>(landingPageOption)
  const [workingContent, setWorkingContent] = useState<SiteContent>(siteContent)
  const [showVersionSelector, setShowVersionSelector] = useState(false)
  const localContent = workingContent.localizedSiteContents.find(lsc => lsc.language === selectedLanguage)
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

  /** updates the global SiteContent object with the given HtmlPage, which may be associated with a navItem */
  const updatePage = (page: HtmlPage, navItemId?: string) => {
    if (!localContent) {
      return
    }
    let updatedLocalContent
    if (!navItemId) {
      updatedLocalContent = {
        ...localContent,
        landingPage: page
      }
    } else {
      const updatedNavBarItems = [...localContent.navbarItems]
      const matchedNavItem = navBarInternalItems.find(navItem => navItem.id === navItemId)
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
  const isEditable = !readOnly && portalEnv.environmentName === 'sandbox'

  const currentNavBarItem = selectedNavOpt.value ? navBarInternalItems
    .find(navItem => navItem.id === selectedNavOpt.value) : null
  const pageToRender = currentNavBarItem ? currentNavBarItem.htmlPage : localContent.landingPage

  const pageOpts: {label: string, value: string | null}[] = navBarInternalItems
    .map(navItem => ({ label: navItem.text, value: navItem.id }))
  pageOpts.unshift({ label: 'Landing page', value: null })

  return <div className="d-flex bg-white p-3">
    <div className="ps-3">
      <div className="d-flex mb-2 align-items-baseline">
        <h2 className="h5">Website content</h2>
        <div className="ms-3 text-muted">
          version {siteContent.version}
        </div>
        {isEditable && <button className="btn btn-secondary"
          onClick={() => setShowVersionSelector(!showVersionSelector)}>select</button> }
      </div>
      <div className="d-flex mb-3 w-100">
        <div>
          <Select options={pageOpts} value={selectedNavOpt}
            onChange={e => setSelectedNavOpt(e ?? landingPageOption)}/>
        </div>
        {
          isEditable && <>
            <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
              <FontAwesomeIcon icon={faPlus}/> Add page
            </button>
            <Button className="ms-auto" variant="primary"
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
      <div>
        {pageToRender &&
          <ApiProvider api={previewApi}>
            <HtmlPageEditView htmlPage={pageToRender} readOnly={readOnly}
              updatePage={page => updatePage(page, currentNavBarItem?.id)}/>
          </ApiProvider>}
      </div>
    </div>
    { showVersionSelector &&
        <SiteContentVersionSelector portalShortcode={portalShortcode} stableId={siteContent.stableId}
          current={siteContent} loadSiteContent={loadSiteContent} portalEnv={portalEnv}
          switchToVersion={switchToVersion}
          onDismiss={() => setShowVersionSelector(false)}/>
    }
  </div>
}

export default SiteContentEditor

