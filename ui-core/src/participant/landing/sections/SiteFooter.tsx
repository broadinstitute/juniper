import classNames from 'classnames'
import { HtmlSectionView } from './HtmlSectionView'
import { Link } from 'react-router-dom'
import React from 'react'
import { HtmlSection } from 'src/types/landingPageConfig'

/**
 * Returns a siteFooter section for use in the Participant UI and Admin UI site previews
 */
export function SiteFooter({ footerSection }: { footerSection?: HtmlSection }) {
  const hasFooter = !!footerSection

  return <footer>
    <div
      className={classNames('row mx-0 d-flex justify-content-center', { 'pt-5': hasFooter })}
    >
      <div className="col-12 col-lg-8 px-0">
        {footerSection && (
          <HtmlSectionView section={footerSection}/>
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
}
