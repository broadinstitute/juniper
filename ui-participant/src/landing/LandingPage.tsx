import classNames from 'classnames'
import React, { useEffect, useRef } from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { LocalSiteContent } from '@juniper/ui-core'
import _uniqueId from 'lodash/uniqueId'
import Navbar from '../Navbar'
import { MailingListModal } from '@juniper/ui-core/build/participant/landing/MailingListModal'
import { HtmlSectionView } from '@juniper/ui-core/build/participant/landing/sections/HtmlSectionView'

/** renders the landing page for a portal (e.g. hearthive.org) */
function LandingPageView({ localContent }: { localContent: LocalSiteContent }) {
  const location = useLocation()
  // we don't use useId() since this needs to be used in a CSS selector
  // see https://blog.openreplay.com/understanding-the-useid-hook-in-react/
  const mailingListModalId = useRef(_uniqueId('mailingListModel'))

  useEffect(() => {
    const mailingListLinks = document.querySelectorAll<HTMLLinkElement>('a[href="#mailing-list"]')
    Array.from(mailingListLinks).forEach(el => {
      el.dataset.bsToggle = 'modal'
      el.dataset.bsTarget = `#${mailingListModalId.current}`
    })
  }, [location.pathname])

  const hasFooter = !!localContent.footerSection

  return <div className="LandingPage">
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <Navbar />
      <main className="flex-grow-1">
        <Outlet/>
      </main>
      <footer>
        <div
          className={classNames('row mx-0 d-flex justify-content-center', { 'pt-5': hasFooter })}
        >
          <div className="col-12 col-lg-8 px-0">
            {localContent.footerSection && (
              <HtmlSectionView section={localContent.footerSection}/>
            )}
            <div className="row mx-0">
              <div
                className={classNames('col-12', { 'border-top border-secondary': hasFooter })}
                style={{
                  paddingTop: '2rem', paddingBottom: '2rem',
                  marginTop: hasFooter ? '6rem' : 0
                }}
              >
                <Link to="/privacy">Privacy Policy</Link>
                <Link to="/terms/participant" style={{ marginLeft: '2rem' }}>Terms of Use</Link>
              </div>
            </div>
          </div>
        </div>
      </footer>
      <MailingListModal id={mailingListModalId.current} />
    </div>
  </div>
}

export default LandingPageView
