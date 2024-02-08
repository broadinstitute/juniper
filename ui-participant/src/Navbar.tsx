import { faGlobe, faUser } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Collapse } from 'bootstrap'
import classNames from 'classnames'
import React, { useEffect, useId, useRef } from 'react'
import { Link, NavLink, useLocation } from 'react-router-dom'
import { HashLink } from 'react-router-hash-link'

import Api, { getEnvSpec, getImageUrl, NavbarItem, PortalStudy } from 'api/api'
import { MailingListModal, PortalLanguage } from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { useConfig } from 'providers/ConfigProvider'
import { getOidcConfig } from 'authConfig'
import { UserManager } from 'oidc-client-ts'
import { uniqueId } from 'lodash'

const navLinkClasses = 'nav-link fs-5 ms-lg-3'

type NavbarProps = JSX.IntrinsicElements['nav']

/** renders the navbar for participants */
export default function Navbar(props: NavbarProps) {
  const portalEnv = usePortalEnv()
  const { localContent } = portalEnv
  const config = useConfig()
  const { user, logoutUser, selectedLanguage, changeLanguage } = useUser()
  const envSpec = getEnvSpec()
  const navLinks = localContent.navbarItems

  const languageOptions = portalEnv.portal.portalLanguages

  /** invoke B2C change password flow */
  function doChangePassword() {
    const oidcConfig = getOidcConfig(config.b2cTenantName, config.b2cClientId, config.b2cChangePasswordPolicyName)
    const userManager = new UserManager(oidcConfig)
    userManager.signinRedirect({
      redirectMethod: 'replace',
      extraQueryParams: { portalShortcode: envSpec.shortcode as string }
    })
  }

  /** send a logout to the api then logout */
  function doLogout() {
    Api.logout().then(() => {
      logoutUser()
      window.location.href = '/'
    })
  }

  const dropdownRef = useRef<HTMLDivElement | null>(null)
  const location = useLocation()
  useEffect(() => {
    if (dropdownRef.current) {
      Collapse.getInstance(dropdownRef.current)?.hide()
    }
  }, [location.pathname])

  const dropdownId = uniqueId('navDropdown')

  return <nav {...props} className={classNames('navbar navbar-expand-lg navbar-light', props.className)}>
    <div className="container-fluid">
      <NavLink to="/" className="navbar-brand">
        <img className="Navbar-logo" style={{ maxHeight: '30px' }}
          src={getImageUrl(localContent.navLogoCleanFileName, localContent.navLogoVersion)} alt="logo"/>
      </NavLink>
      <button
        aria-controls={dropdownId} aria-expanded="false" aria-label="Toggle navigation"
        className="navbar-toggler"
        data-bs-toggle="collapse" data-bs-target={`#${dropdownId}`}
        type="button"
      >
        <span className="navbar-toggler-icon"/>
      </button>
      <div ref={dropdownRef} className="collapse navbar-collapse mt-2 mt-lg-0" id={dropdownId}>
        <ul className="navbar-nav">
          {navLinks.map((navLink: NavbarItem, index: number) => <li key={index} className="nav-item">
            <CustomNavLink navLink={navLink}/>
          </li>)}
        </ul>
        <ul className="navbar-nav ms-auto">
          <LanguageDropdown
            languageOptions={languageOptions}
            selectedLanguage={selectedLanguage}
            changeLanguage={changeLanguage}
            reloadPortal={() => window.location.reload()}
          />
          {user.isAnonymous && (
            <>
              <li className="nav-item">
                <NavLink
                  className={classNames(
                    'btn btn-lg btn-outline-primary',
                    'd-flex justify-content-center',
                    'mb-3 mb-lg-0 ms-lg-3'
                  )}
                  to="/hub"
                >
                    Log In
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink
                  className={classNames(
                    'btn btn-lg btn-primary',
                    'd-flex justify-content-center',
                    'mb-3 mb-lg-0 ms-lg-3'
                  )}
                  to={getMainJoinLink(portalEnv.portal.portalStudies)}
                >
                    Join
                </NavLink>
              </li>
            </>
          )}
          {!user.isAnonymous && (
            <>
              <li className="nav-item">
                <Link
                  className={classNames(
                    'btn btn-lg btn-outline-primary',
                    'd-flex justify-content-center',
                    'ms-lg-3'
                  )}
                  to="/hub"
                >
                    Dashboard
                </Link>
              </li>
              <li className="nav-item dropdown d-flex flex-column">
                <button
                  aria-expanded="false"
                  aria-label={user.username}
                  className={classNames(
                    navLinkClasses,
                    'btn btn-text dropdown-toggle text-start'
                  )}
                  data-bs-toggle="dropdown"
                >
                  <FontAwesomeIcon className="d-none d-lg-inline" icon={faUser}/>
                  <span className="d-lg-none">{user.username}</span>
                </button>
                <div className="dropdown-menu dropdown-menu-end">
                  <p
                    className="d-none d-lg-block"
                    style={{
                      padding: 'var(--bs-dropdown-item-padding-y) var(--bs-dropdown-item-padding-x)',
                      margin: 0,
                      fontWeight: 400,
                      color: 'var(--bs-dropdown-link-color)',
                      whiteSpace: 'nowrap'
                    }}
                  >
                    {user.username}
                  </p>
                  <hr className="dropdown-divider d-none d-lg-block"/>
                  <button className="dropdown-item" onClick={doChangePassword}>
                      Change Password
                  </button>
                  <button className="dropdown-item" onClick={doLogout}>
                      Log Out
                  </button>
                </div>
              </li>
            </>
          )}
        </ul>
      </div>
    </div>
  </nav>
}

type MailingListNavLinkProps = JSX.IntrinsicElements['a']

const MailingListNavLink = (props: MailingListNavLinkProps) => {
  const modalId = useId()

  return (
    <>
      <a
        {...props}
        data-bs-toggle="modal"
        data-bs-target={`#${CSS.escape(modalId)}`}
      />
      <MailingListModal id={modalId}/>
    </>
  )
}

/** renders a single navBarItem. This will likely get split out into subcomponents for each type as they are
 * implemented
 */
export function CustomNavLink({ navLink }: { navLink: NavbarItem }) {
  if (navLink.itemType === 'INTERNAL') {
    // we require navbar links to be absolute rather than relative links
    return <NavLink to={`/${navLink.htmlPage.path}`} className={navLinkClasses}>{navLink.text}</NavLink>
  } else if (navLink.itemType === 'INTERNAL_ANCHOR') {
    return <HashLink to={navLink.href} className={navLinkClasses}>{navLink.text}</HashLink>
  } else if (navLink.itemType === 'MAILING_LIST') {
    return <MailingListNavLink role="button" className={navLinkClasses}>{navLink.text}</MailingListNavLink>
  } else if (navLink.itemType === 'EXTERNAL') {
    return <a href={navLink.href} className={navLinkClasses} target="_blank">{navLink.text}</a>
  }
  return <></>
}

/** the default join link -- will be rendered in the top right corner */
export const getMainJoinLink = (portalStudies: PortalStudy[]) => {
  const joinable = filterUnjoinableStudies(portalStudies)
  /** if there's only one joinable study, link directly to it */
  const joinPath = joinable.length === 1
    ? `/studies/${joinable[0].study.shortcode}/join`
    : '/join'
  return joinPath
}

/** filters out studies that are not accepting enrollment */
export const filterUnjoinableStudies = (portalStudies: PortalStudy[]): PortalStudy[] => {
  return portalStudies.filter(pStudy =>
    pStudy.study.studyEnvironments[0].studyEnvironmentConfig.acceptingEnrollment)
}

/**
 *
 */
export function LanguageDropdown({ languageOptions, selectedLanguage, changeLanguage, reloadPortal }: {
  languageOptions: PortalLanguage[],
  selectedLanguage: string,
  changeLanguage: (languageCode: string) => void,
  reloadPortal: () => void
}) {
  return (
    languageOptions.length > 1 ? (
      <li className="nav-item dropdown d-flex flex-column">
        <button
          aria-expanded="false"
          aria-label="Select a language"
          className={classNames(
            navLinkClasses,
            'btn btn-text dropdown-toggle text-start'
          )}
          data-bs-toggle="dropdown"
        >
          <FontAwesomeIcon className="d-none d-lg-inline mx-1" icon={faGlobe}/>
          {languageOptions.find(l => l.languageCode === selectedLanguage)?.languageName}
          <span className="d-lg-none">Select a language</span>
        </button>
        <div className="dropdown-menu dropdown-menu-end">
          {languageOptions.map((lang, index) => {
            return (
              <button key={index} className="dropdown-item" aria-label={lang.languageName}
                onClick={() => {
                  changeLanguage(lang.languageCode)
                  reloadPortal()
                }}>
                {lang.languageName}
              </button>
            )
          })}
        </div>
      </li>) : null
  )
}
