import React, { useState } from 'react'
import { NavbarItemInternal, PortalEnvironment } from 'api/api'
import Select from 'react-select'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import HtmlPageEditView from './HtmlPageEditView'
import { HtmlPage, LocalSiteContent, ApiProvider, SiteContent } from '@juniper/ui-core'
import { Link } from 'react-router-dom'
import { ApiContextT } from '@juniper/ui-core/build/participant/ApiProvider'

type NavbarOption = {label: string, value: string | null}
const landingPageOption = { label: 'Landing page', value: null }

/** shows a site content in editable form with a live preview.  Defaults to english-only for now */
export const InitializedSiteContentView = ({ siteContent, previewApi }:
                                      {siteContent: SiteContent, previewApi: ApiContextT}) => {
  const selectedLanguage = 'en'
  const [selectedNavOpt, setSelectedNavOpt] = useState<NavbarOption>(landingPageOption)
  const [workingContent, setWorkingContent] = useState<SiteContent>(siteContent)

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

  const currentNavBarItem = selectedNavOpt.value ? navBarInternalItems
    .find(navItem => navItem.id === selectedNavOpt.value) : null
  const pageToRender = currentNavBarItem ? currentNavBarItem.htmlPage : localContent.landingPage

  const pageOpts: {label: string, value: string | null}[] = navBarInternalItems
    .map(navItem => ({ label: navItem.text, value: navItem.id }))
  pageOpts.unshift({ label: 'Landing page', value: null })

  return <div className="d-flex bg-white p-3">
    <div className="ps-3">
      <div className="d-flex mb-3 w-100">
        <div>
          <Select options={pageOpts} value={selectedNavOpt}
            onChange={e => setSelectedNavOpt(e ?? landingPageOption)}/>
        </div>
        <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
          <FontAwesomeIcon icon={faPlus}/> Add page
        </button>
        <button className="btn btn-primary ms-auto" onClick={() => alert('not yet implemented')}>Save</button>
        <Link className="btn btn-secondary" to={'../..'}>Cancel</Link>
      </div>
      <div>
        {pageToRender &&
          <ApiProvider api={previewApi}>
            <HtmlPageEditView htmlPage={pageToRender}
              updatePage={page => updatePage(page, currentNavBarItem?.id)}/>
          </ApiProvider>}
      </div>
    </div>
  </div>
}


/** shows a view for editing site content pages */
const SiteContentView = ({ portalEnv, portalShortcode }: {portalEnv: PortalEnvironment, portalShortcode: string}) => {
  if (!portalEnv.siteContent) {
    return <div>no site content configured yet</div>
  }
  /** uses the admin image retrieval endpoint */
  const getImageUrl = (cleanFileName: string, version: number) =>
    `/api/public/portals/v1/${portalShortcode}/env/${portalEnv.environmentName}/siteImages/${version}/${cleanFileName}`
  const previewApi: ApiContextT = {
    getImageUrl,
    submitMailingListContact: () => Promise.resolve({})
  }

  return <InitializedSiteContentView siteContent={portalEnv.siteContent} previewApi={previewApi}/>
}

export default SiteContentView
