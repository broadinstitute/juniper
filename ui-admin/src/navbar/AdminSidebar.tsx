import React, { useState } from 'react'
import { useUser } from '../user/UserProvider'
import { Link, NavLink, useParams } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRightFromBracket } from '@fortawesome/free-solid-svg-icons'
import { Study } from '@juniper/ui-core'
import { studyShortcodeFromPath } from '../study/StudyRouter'
import { useNavContext } from './NavContextProvider'
import { StudySidebar } from './StudySidebar'
import CollapsableMenu from './CollapsableMenu'
import { Config } from '../api/api'
import { Button } from '../components/forms/Button'
import classNames from 'classnames'

const ZONE_COLORS: { [index: string]: string } = {
  'dev': 'rgb(70 143 124)', // dark green
  'local': 'rgb(23 26 30)', // grey
  'prod': '#333F52' // blue (default)
}

export const sidebarNavLinkClasses = 'text-white p-1 rounded w-100 d-block sidebar-nav-link'

/** renders the left navbar of admin tool */
const AdminSidebar = ({ config }: { config: Config }) => {
  const HIDE_SIDEBAR_KEY = 'adminSidebar.hide'

  const [open, setOpen] = useState(!(localStorage.getItem(HIDE_SIDEBAR_KEY) === 'true'))
  const { user } = useUser()
  const params = useParams()

  const { portalList } = useNavContext()
  const studyShortcode = studyShortcodeFromPath(params['*'])
  const portalShortcode = params.portalShortcode

  let studyList: Study[] = []
  if (portalList.length) {
    studyList = portalList.flatMap(portal => portal.portalStudies.map(ps => ps.study))
  }

  if (!user || (!user.superuser && !portalShortcode)) {
    return <div></div>
  }
  const currentStudy = studyList.find(study => study.shortcode === studyShortcode)

  const color = ZONE_COLORS[config.deploymentZone] || ZONE_COLORS['prod']

  return <div style={{ backgroundColor: color, minHeight: '100vh', minWidth: open ? '250px' : '50px' }}
    className="p-2 pt-3">

    {!open && <Button variant="secondary" className="m-1 text-light" tooltipPlacement={'right'}
      onClick={() => {
        setOpen(!open)
        localStorage.setItem(HIDE_SIDEBAR_KEY, (!open).toString())
      }}
      tooltip={open ? 'Hide sidebar' : 'Show sidebar'}>
      <FontAwesomeIcon icon={faArrowRightFromBracket}
        className={classNames(open ? 'fa-rotate-180' : '')}/>
    </Button> }
    {open && <>
      <div className="d-flex justify-content-between align-items-center">
        <Link to="/" className="text-white fs-4 px-2 rounded-1 sidebar-nav-link flex-grow-1">Juniper</Link>
        <Button variant="secondary" className="m-1 text-light" tooltipPlacement={'right'}
          onClick={() => {
            setOpen(!open)
            localStorage.setItem(HIDE_SIDEBAR_KEY, (!open).toString())
          }}
          tooltip={open ? 'Hide sidebar' : 'Show sidebar'}>
          <FontAwesomeIcon icon={faArrowRightFromBracket}
            className={classNames(open ? 'fa-rotate-180' : '')}/>
        </Button>
      </div>
      { currentStudy && <StudySidebar study={currentStudy} portalList={portalList}
        portalShortcode={portalShortcode!}/> }

      {user?.superuser && <CollapsableMenu header={'Superuser functions'} content={
        <ul className="list-unstyled">
          <li className="mb-2">
            <NavLink to="/users" className={sidebarNavLinkClasses}>All users</NavLink>
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
        </ul>}/>}
    </>}
  </div>
}

export default AdminSidebar
