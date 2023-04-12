import React, { useState } from 'react'
import { HtmlPage, NavbarItem, PortalEnvironment } from '../../api/api'

const SiteContentView = ({ portalEnv }: {portalEnv: PortalEnvironment}) => {
  const selectedLanguage = 'en'
  const [selectedNavItem, setSelectedNavItem] = useState<NavbarItem | null>(null)

  if (!portalEnv.siteContent) {
    return <div>no site content configured yet</div>
  }
  const localContent = portalEnv.siteContent?.localizedSiteContents.find(lsc => lsc.language === selectedLanguage)
  if (!localContent) {
    return <div>no content for language {selectedLanguage}</div>
  }
  const renderedTitle = selectedNavItem ? selectedNavItem.label : 'Landing page'
  const pageToRender = selectedNavItem ? selectedNavItem.htmlPage : localContent.landingPage

  return <div className="container d-flex bg-white p-3">
    <ul className="list-group">
      <li className="list-group-item" onClick={() => setSelectedNavItem(null)}>Landing page</li>
      {localContent.navbarItems.map(navItem => <li key={navItem.label} className="list-group-item" role="button"
        onClick={() => setSelectedNavItem(navItem)}>
        {navItem.label}
      </li>
      )}
    </ul>
    <div className="ps-3">
      <h2 className="h5">{renderedTitle}</h2>
      <div>
        {pageToRender && <HtmlPageView htmlPage={pageToRender}/>}
      </div>
    </div>
  </div>
}

const HtmlPageView = ({ htmlPage }: {htmlPage: HtmlPage}) => {
  return <div>
    {htmlPage.sections.map((section, index) => {
      const textValue = JSON.stringify(JSON.parse(section?.sectionConfig ?? '{}'), null, 2)
      return <div key={index}>
        <textarea readOnly value={textValue} rows={8} cols={120}/>
      </div>
    })}
  </div>
}

export default SiteContentView
