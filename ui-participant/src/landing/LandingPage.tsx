import React, { useEffect, useId } from 'react'
import { Outlet } from 'react-router-dom'
import LandingNavbar from './LandingNavbar'
import { LocalSiteContent } from 'api/api'
import { MailingListModal } from './MailingListModal'
import { HtmlSectionView } from './sections/HtmlPageView'

/** renders the landing page for a portal (e.g. hearthive.org) */
function LandingPageView({ localContent }: { localContent: LocalSiteContent }) {
  const mailingListModalId = useId()

  useEffect(() => {
    const mailingListLinks = document.querySelectorAll<HTMLLinkElement>('a[href="#mailing-list"]')
    Array.from(mailingListLinks).forEach(el => {
      el.dataset.bsToggle = 'modal'
      el.dataset.bsTarget = `#${CSS.escape(mailingListModalId)}`
    })
  }, [])

  return <div className="LandingPage">
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <div>
        <LandingNavbar aria-label="Primary" />
      </div>
      <main className="flex-grow-1">
        <Outlet/>
      </main>
      {localContent.footerSection && <footer>
        <HtmlSectionView section={localContent.footerSection}/>
      </footer>}
      <MailingListModal id={mailingListModalId} />
    </div>
  </div>
}

export default LandingPageView
