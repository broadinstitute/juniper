import React, { useState } from 'react'
import { useUser } from '../user/UserProvider'
import { Link, NavLink, useParams } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowLeft, faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { Study } from '@juniper/ui-core'
import { studyShortcodeFromPath } from '../study/StudyRouter'
import { useNavContext } from './NavContextProvider'
import { StudySidebar } from './StudySidebar'
import CollapsableMenu from './CollapsableMenu'

/** renders the left navbar of admin tool */
const AdminSidebar = () => {
  const [open, setOpen] = useState(true)
  const { user } = useUser()
  const params = useParams()

  const { portalList } = useNavContext()
  const studyShortcode = studyShortcodeFromPath(params['*'])
  const portalShortcode = params.portalShortcode

  let studyList: Study[] = []
  if (portalList.length) {
    studyList = portalList.flatMap(portal => portal.portalStudies.map(ps => ps.study))
  }

  if (user.isAnonymous) {
    return <div></div>
  }
  const currentStudy = studyList.find(study => study.shortcode === studyShortcode)

  return <div style={{ backgroundColor: '#333F52', minHeight: '100vh', minWidth: open ? '250px' : '50px' }}
    className="p-2 pt-3">

    {!open &&  <button onClick={() => setOpen(!open)} title="toggle sidebar" className="btn btn-link text-white">
      <FontAwesomeIcon icon={faArrowRight}/>
    </button>}
    {open && <>
      <div className="d-flex justify-content-between align-items-center">
        <Link to="/" className="text-white fs-4 ms-2">Juniper</Link>
        <button onClick={() => setOpen(!open)} title="toggle sidebar" className="btn btn-link text-white">
          <FontAwesomeIcon icon={faArrowLeft}/>
        </button>
      </div>
      { currentStudy && <StudySidebar study={currentStudy} portalList={portalList}
        portalShortcode={portalShortcode!}/> }

      {user.superuser && <CollapsableMenu header={'Superuser functions'} content={
        <ul className="list-unstyled">
          <li>
            <NavLink to="/users" className="text-white">All users</NavLink>
          </li>
        </ul>}/>}
    </>}
  </div>
}

export default AdminSidebar
