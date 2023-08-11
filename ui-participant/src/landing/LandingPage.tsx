import classNames from 'classnames'
import React, { useEffect, useRef } from 'react'
import {Link, Outlet, useLocation, useSearchParams} from 'react-router-dom'
import { LocalSiteContent, MailingListModal, HtmlSectionView } from '@juniper/ui-core'
import _uniqueId from 'lodash/uniqueId'
import Navbar from '../Navbar'
import * as bootstrap from 'bootstrap'

export const MAILING_LIST_QUERY_PARAM = 'showJoinMailingList'

/** renders the landing page for a portal (e.g. hearthive.org) */
function LandingPageView({ localContent }: { localContent: LocalSiteContent }) {
  const location = useLocation()
  // we don't use useId() since this needs to be used in a CSS selector
  // see https://blog.openreplay.com/understanding-the-useid-hook-in-react/
  const mailingListModalId = useRef(_uniqueId('mailingListModel'))
  const [searchParams] = useSearchParams()
  const mailingList = searchParams.get(MAILING_LIST_QUERY_PARAM)

  useEffect(() => {
    const mailingListLinks = document.querySelectorAll<HTMLLinkElement>('a[href="#mailing-list"]')
    Array.from(mailingListLinks).forEach(el => {
      el.dataset.bsToggle = 'modal'
      el.dataset.bsTarget = `#${mailingListModalId.current}`
    })
  }, [location.pathname])

  useEffect(() => {
    /** if the mailingList query param is present, auto-trigger the modal */
    if (mailingList !== 'true') {
      return
    }
    const modalEl = document.querySelector(`#${mailingListModalId.current}`)
    if (!modalEl) { return }
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl)
    modal.show()
  }, [])

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
