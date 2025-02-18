import React, {
  useEffect,
  useState
} from 'react'
import { useUser } from '../user/UserProvider'
import {
  Link,
  NavLink,
  useParams
} from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCaretRight,
  faPencil,
  faSave
} from '@fortawesome/free-solid-svg-icons'
import { Study } from '@juniper/ui-core'
import { studyShortcodeFromPath } from 'study/StudyRouter'
import { useNavContext } from './NavContextProvider'
import { StudySidebar } from './StudySidebar'
import CollapsableMenu from './CollapsableMenu'
import { Config } from 'api/api'
import { Button } from 'components/forms/Button'
import classNames from 'classnames'

const ZONE_COLORS: { [index: string]: string } = {
  'dev': 'rgb(70 143 124)', // dark green
  'local': 'rgb(23 26 30)', // grey
  'prod': '#333F52' // blue (default)
}

export const sidebarNavLinkClasses = 'text-white p-1 rounded w-100 d-block sidebar-nav-link'

export type StudySidebarConfig = {
  hidden: string[]
}

export type SidebarConfig = {
  studyConfig?: { [studyShortcode: string]: StudySidebarConfig }
}

const parseSidebarConfigState = (data: string): SidebarConfig => {
  try {
    return JSON.parse(data)
  } catch (e) {
    return {}
  }
}

/** renders the left navbar of admin tool */
const AdminSidebar = ({ config }: { config: Config }) => {
  const SHOW_SIDEBAR_KEY = 'adminSidebar.show'

  const [open, setOpen] = useState((localStorage.getItem(SHOW_SIDEBAR_KEY) || 'true') === 'true')
  const { user } = useUser()
  const params = useParams()

  const { portalList } = useNavContext()
  const studyShortcode = studyShortcodeFromPath(params['*'])
  const portalShortcode = params.portalShortcode

  let studyList: Study[] = []
  if (portalList.length) {
    studyList = portalList.flatMap(portal => portal.portalStudies.map(ps => ps.study))
  }
  const currentStudy = studyList.find(study => study.shortcode === studyShortcode)
  const color = ZONE_COLORS[config.deploymentZone] || ZONE_COLORS['prod']


  const sidebarConfig: SidebarConfig = parseSidebarConfigState(localStorage.getItem('sidebarConfig') || '{}')

  const [isEditingSidebarConfig, setIsEditingSidebarConfig] = React.useState(false)

  const getStudyConfig = (studyShortcode: string): StudySidebarConfig => {
    if (!sidebarConfig.studyConfig) {
      sidebarConfig.studyConfig = {}
      localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfig))
    }

    if (!sidebarConfig.studyConfig[studyShortcode]) {
      sidebarConfig.studyConfig[studyShortcode] = { hidden: [] }
      localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfig))
    }


    return sidebarConfig.studyConfig[studyShortcode]
  }

  const setStudyConfig = (studyShortcode: string, studyConfig: StudySidebarConfig) => {
    if (!sidebarConfig.studyConfig) {
      sidebarConfig.studyConfig = {}
    }

    sidebarConfig.studyConfig[studyShortcode] = studyConfig
    localStorage.setItem('sidebarConfig', JSON.stringify(sidebarConfig))
  }

  const toggleHiddenItem = (key: string) => {
    if (!currentStudy) {
      return
    }

    const studyConfig = getStudyConfig(currentStudy.shortcode)
    const hiddenItems = studyConfig.hidden
    if (hiddenItems.includes(key)) {
      studyConfig.hidden = hiddenItems.filter(item => item !== key)
    } else {
      studyConfig.hidden = [...hiddenItems, key]
    }
    setStudyConfig(currentStudy.shortcode, studyConfig)
  }

  // automatically collapse the sidebar for mobile-first routes
  useEffect(() => {
    if (isMobileFirstRoute()) {
      setOpen(false)
    }
  }, [])

  if (!user || (!user.superuser && !portalShortcode)) {
    return <div></div>
  }

  return <div style={{ backgroundColor: color, minHeight: '100vh', minWidth: open ? '250px' : '50px' }}
    className="p-2 pt-3 d-flex flex-column">
    <div style={{ minHeight: '100vh', height: '100%' }}>
      <div className="d-flex justify-content-between align-items-center">
        { open && <Link to="/" className="text-white fs-4 px-2 rounded-1 sidebar-nav-link flex-grow-1">Juniper</Link> }
        <Button variant="secondary" className="m-1 text-light" tooltipPlacement={'right'}
          onClick={() => {
            setOpen(!open)
            localStorage.setItem(SHOW_SIDEBAR_KEY, (!open).toString())
          }}
          tooltip={open ? 'Hide sidebar' : 'Show sidebar'}>
          <FontAwesomeIcon icon={faCaretRight}
            className={classNames(open ? 'fa-rotate-180' : '')}/>
        </Button>
      </div>
      { open && <>
        {currentStudy && <StudySidebar
          study={currentStudy}
          isEditingSidebarConfig={isEditingSidebarConfig}
          toggleHiddenItem={toggleHiddenItem}
          studySidebarConfig={getStudyConfig(currentStudy.shortcode)}
          portalList={portalList}
          portalShortcode={portalShortcode!}/> }

        {user?.superuser && <CollapsableMenu header={'Superuser Functions'} content={
          <ul className="list-unstyled">
            <li className="mb-2">
              <NavLink to="/users" className={sidebarNavLinkClasses}>All Users</NavLink>
            </li>
            <li className="mb-2">
              <NavLink to="/populate" className={sidebarNavLinkClasses}>Populate</NavLink>
            </li>
            <li className="mb-2">
              <NavLink to="/integrations" className={sidebarNavLinkClasses}>Integrations</NavLink>
            </li>
            <li className="mb-2">
              <NavLink to="/logEvents" className={sidebarNavLinkClasses}>Log Events</NavLink>
            </li>
            <li className="mb-2">
              <NavLink to="/system/settings" className={sidebarNavLinkClasses}>System Settings</NavLink>
            </li>
          </ul>}/>}
      </>}
    </div>
    {currentStudy && <div className="sticky-bottom w-100 d-flex justify-content-end p-1 pointer-none">
      <button
        className="btn btn-secondary btn-sm text-white hover-opacity-50 pointer-auto"
        onClick={() => setIsEditingSidebarConfig(!isEditingSidebarConfig)}
        aria-label={isEditingSidebarConfig ? 'Save sidebar config' : 'Edit sidebar config'}
      >
        {isEditingSidebarConfig
          ? <FontAwesomeIcon icon={faSave}/>
          : <FontAwesomeIcon icon={faPencil}/>}
      </button>
    </div>}
  </div>
}

const isMobileFirstRoute = () => {
  return window.location.pathname.endsWith('kits/scan')
}

export default AdminSidebar
