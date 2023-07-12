import React, { useState } from 'react'
import { NavbarItemInternal, PortalEnvironment } from 'api/api'
import Select from 'react-select'
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import HtmlPageEditView from "./HtmlPageEditView";
import {HtmlPage, LocalSiteContent, NavbarItem, SiteContent} from "@juniper/ui-core/build/types/landingPageConfig";

type NavbarOption = {label: string, value: NavbarItemInternal | null}
const landingPageOption = {label: 'Landing page', value: null}


const InitializedSiteContentView = ({siteContent}: {siteContent: SiteContent}) => {
  const selectedLanguage = 'en'
  const [selectedNavOpt, setSelectedNavOpt] = useState<NavbarOption>(landingPageOption)
  const [workingContent, setWorkingContent] = useState<SiteContent>(siteContent)

  const localContent = workingContent.localizedSiteContents.find(lsc => lsc.language === selectedLanguage)

  const updateLocalContent = (localContent: LocalSiteContent) => {
    const matchedIndex = workingContent.localizedSiteContents
      .findIndex(lsc => lsc.language === localContent.language)
    workingContent.localizedSiteContents[matchedIndex] = localContent
    setWorkingContent(workingContent)
  }

  const updatePage = (page: HtmlPage, navItem: NavbarItemInternal | null) => {
    if (!localContent) {
      return
    }
    const updatedLocalContents = [...workingContent.localizedSiteContents]
    const updatedIndex = updatedLocalContents.findIndex(lsc => lsc.language === selectedLanguage)
    const updatedLocalContent = updatedLocalContents[updatedIndex]

    if (!navItem) {
      updatedLocalContents[updatedIndex] = {
        ...updatedLocalContent,
        landingPage: page
      }
    } else {
      const updatedNavBarItems = [...updatedLocalContent.navbarItems]
      updatedNavBarItems[navItem.itemOrder] = navItem
      updatedLocalContents[updatedIndex] = {
        ...updatedLocalContent,
        navbarItems: updatedNavBarItems
      }
    }
    const newWorkingContent: SiteContent = {
      ...workingContent,
      localizedSiteContents: updatedLocalContents
    }
    setWorkingContent(newWorkingContent)
  }


  if (!localContent) {
    return <div>no content for language {selectedLanguage}</div>
  }
  const currentNavBarItem = selectedNavOpt.value ? selectedNavOpt.value : null
  const pageToRender = currentNavBarItem ? currentNavBarItem.htmlPage : localContent.landingPage

  const pageOpts: {label: string, value: NavbarItemInternal | null}[] = localContent.navbarItems
    .filter((navItem): navItem is NavbarItemInternal => navItem.itemType === 'INTERNAL')
    .map(navItem => ({label: navItem.text, value: navItem}))
  pageOpts.unshift({label: 'Landing page', value: null})

  return <div className="d-flex bg-white p-3">
    <div className="ps-3">
      <div className="d-flex mb-3">
        <div>
          <Select options={pageOpts} value={selectedNavOpt}
                  onChange={e => setSelectedNavOpt(e ?? landingPageOption)}/>
        </div>
        <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
          <FontAwesomeIcon icon={faPlus}/> Add page
        </button>
      </div>


      <div>
        {pageToRender && <HtmlPageEditView htmlPage={pageToRender}
                                           updatePage={(page) => updatePage(page, currentNavBarItem)}/>}
      </div>
    </div>
  </div>
}


/** shows a view for editing site content pages */
const SiteContentView = ({ portalEnv }: {portalEnv: PortalEnvironment}) => {
  if (!portalEnv.siteContent) {
    return <div>no site content configured yet</div>
  }
  return <InitializedSiteContentView siteContent={portalEnv.siteContent}/>
}

export default SiteContentView
