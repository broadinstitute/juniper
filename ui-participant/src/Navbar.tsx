import {
  faGlobe,
  faUser
} from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Collapse } from 'bootstrap'
import classNames from 'classnames'
import React, {
  useEffect,
  useId,
  useRef
} from 'react'
import {
  Link,
  NavLink,
  useLocation,
  useNavigate,
  useSearchParams
} from 'react-router-dom'
import { HashLink } from 'react-router-hash-link'

import Api, {
  getEnvSpec,
  getImageUrl,
  NavbarItem,
  PortalEnvironmentConfig,
  PortalStudy
} from 'api/api'
import {
  MailingListModal,
  PortalEnvironmentLanguageOpt,
  useI18n
} from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { useConfig } from 'providers/ConfigProvider'
import { uniqueId } from 'lodash'
import { UserManager } from 'oidc-client-ts'
import { getOidcConfig } from './authConfig'
import { useActiveUser } from './providers/ActiveUserProvider'
import mixpanel from 'mixpanel-browser'

const navLinkClasses = 'nav-link fs-5 ms-lg-3'

type NavbarProps = JSX.IntrinsicElements['nav']

/** renders the navbar for participants */
export default function Navbar(props: NavbarProps) {
  const { portal, portalEnv, reloadPortal, localContent } = usePortalEnv()
  const { i18n, selectedLanguage, changeLanguage } = useI18n()
  const { ppUser, profile } = useActiveUser()
  const { user } = useUser()
  const navLinks = localContent.navbarItems

  async function updatePreferredLanguage(selectedLanguage: string) {
    //track the language change
    mixpanel.track('languageUpdated', { language: selectedLanguage, source: 'navbar' })
    if (profile && ppUser) {
      await Api.updateProfile({
        profile: { ...profile, preferredLanguage: selectedLanguage },
        ppUserId: ppUser.id
      })
    }
  }

  //If the logged-in participant has chosen a preferred language, set the language to that
  useEffect(() => {
    if (profile?.preferredLanguage) {
      changeLanguage(profile.preferredLanguage)
    }
  }, [])

  const changeLanguageAndUpdate = (languageCode: string) => {
    changeLanguage(languageCode)
    updatePreferredLanguage(languageCode)
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
        <img className="Navbar-logo" style={{ height: '30px', maxHeight: '30px' }}
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
            languageOptions={portalEnv.supportedLanguages}
            selectedLanguage={selectedLanguage}
            changeLanguage={changeLanguageAndUpdate}
            reloadPortal={reloadPortal}
          />
          {!user && (
            <>
              <li className="nav-item">
                <NavLink
                  className={classNames(
                    'btn btn-lg btn-outline-primary',
                    'd-flex justify-content-center',
                    'mb-3 mb-lg-0 ms-lg-3'
                  )}
                  onClick={() => mixpanel.track('userLogin', { source: 'navbar' })}
                  to="/hub"
                >
                  {i18n('navbarLogin')}
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink
                  className={classNames(
                    'btn btn-lg btn-primary',
                    'd-flex justify-content-center',
                    'mb-3 mb-lg-0 ms-lg-3'
                  )}
                  to={getMainJoinLink(portal.portalStudies, portalEnv.portalEnvironmentConfig)}
                >
                  {i18n('navbarJoin')}
                </NavLink>
              </li>
            </>
          )}
          {user && <>
            <li className="nav-item">
              <Link
                className={classNames(
                  'btn btn-lg btn-outline-primary',
                  'd-flex justify-content-center',
                  'ms-lg-3'
                )}
                to="/hub"
              >
                {i18n('navbarDashboard')}
              </Link>
            </li>
            <AccountOptionsDropdown/>
          </>
          }
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

/**
 * Returns the join link for a specific study
 */
export const getJoinLink = (studyShortcode: string, opts?: { isProxyEnrollment?: boolean, ppUserId?: string }) => {
  const { isProxyEnrollment, ppUserId } = opts || {}
  const joinPath = `/studies/${studyShortcode}/join`
  if (isProxyEnrollment || ppUserId) {
    return `${joinPath}?${isProxyEnrollment ? 'isProxyEnrollment=true' : ''}${ppUserId ? `&ppUserId=${ppUserId}` : ''}`
  }
  return joinPath
}

/** the default join link -- will be rendered in the top right corner */
export const getMainJoinLink = (portalStudies: PortalStudy[], portalEnvConfig: PortalEnvironmentConfig) => {
  // if there's a primary study, link to it
  if (portalEnvConfig.primaryStudy) {
    return getJoinLink(portalEnvConfig.primaryStudy)
  }
  const joinable = filterUnjoinableStudies(portalStudies)
  /** if there's only one joinable study, link directly to it */
  const joinPath = joinable.length === 1
    ? getJoinLink(joinable[0].study.shortcode)
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
  languageOptions: PortalEnvironmentLanguageOpt[],
  selectedLanguage: string,
  changeLanguage: (languageCode: string) => void,
  reloadPortal: () => void
}) {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const langQueryParam = searchParams.get('lang')

  useEffect(() => {
    if (langQueryParam && langQueryParam !== selectedLanguage &&
        languageOptions.map(l => l.languageCode).includes(langQueryParam)) {
      changeLanguage(langQueryParam)
      reloadPortal()
    }
  }, [langQueryParam])

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
                  //If the user selects a language from the dropdown, remove the lang query param from the URL
                  if (langQueryParam) {
                    searchParams.delete('lang')
                    navigate({ search: searchParams.toString() })
                  }
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

/**
 * User account dropdown menu, with options to edit profile, change password, and log out
 */
export const AccountOptionsDropdown = () => {
  const { user, logoutUser, proxyRelations } = useUser()
  const { i18n, selectedLanguage } = useI18n()
  const config = useConfig()
  const envSpec = getEnvSpec()


  /** invoke B2C change password flow */
  function doChangePassword() {
    const oidcConfig = getOidcConfig(config.b2cTenantName, config.b2cClientId, config.b2cChangePasswordPolicyName)
    const userManager = new UserManager(oidcConfig)
    userManager.signinRedirect({
      redirectMethod: 'replace',
      extraQueryParams: {
        originUrl: window.location.origin,
        portalEnvironment: envSpec.envName,
        portalShortcode: envSpec.shortcode as string,
        // eslint-disable-next-line camelcase
        ui_locales: selectedLanguage
      }
    })
  }

  /** send a logout to the api then logout */
  function doLogout() {
    logoutUser()
  }

  if (!user) {
    return <></>
  }

  return (
    <>
      <li className="nav-item dropdown d-flex flex-column">
        <button
          aria-expanded="false"
          aria-label={`account options for ${user.username}`}
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
          {proxyRelations.length === 0 ? <NavLink to="/hub/profile">
            <button className="dropdown-item" aria-label="edit profile">
              {i18n('profile')}
            </button>
          </NavLink> : <NavLink to="/hub/manageProfiles">
            <button className="dropdown-item" aria-label="manage profiles">
              {i18n('manageProfiles')}
            </button>
          </NavLink>}
          <button className="dropdown-item" aria-label="change password" onClick={() => {
            mixpanel.track('changePassword', { source: 'navbar' })
            doChangePassword()
          }}>
            {i18n('navbarChangePassword')}
          </button>
          <button className="dropdown-item" aria-label="log out" onClick={() => {
            mixpanel.track('userLogout', { source: 'navbar' })
            doLogout()
          }}>
            {i18n('navbarLogout')}
          </button>
        </div>
      </li>
    </>
  )
}
