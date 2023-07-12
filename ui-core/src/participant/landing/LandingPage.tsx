import classNames from 'classnames'
import React, { useEffect, useId } from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { LocalSiteContent } from 'src/types/landingPageConfig'
import Navbar from '../Navbar'
import { MailingListModal } from './MailingListModal'
import { HtmlSectionView } from './sections/HtmlPageView'

/** renders the landing page for a portal (e.g. hearthive.org) */
function LandingPageView({ localContent }: { localContent: LocalSiteContent }) {
  const location = useLocation()
  const mailingListModalId = useId()

  useEffect(() => {
    const mailingListLinks = document.querySelectorAll<HTMLLinkElement>('a[href="#mailing-list"]')
    Array.from(mailingListLinks).forEach(el => {
      el.dataset.bsToggle = 'modal'
      el.dataset.bsTarget = `#${CSS.escape(mailingListModalId)}`
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
      <MailingListModal id={mailingListModalId} />
    </div>
  </div>
}

export default LandingPageView
