import React, {useEffect, useId, useRef, useState} from 'react'
import { useUser } from '../user/UserProvider'
import {Link, NavLink, NavLinkProps, useParams} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowLeft, faArrowRight, faChevronDown, faChevronUp} from "@fortawesome/free-solid-svg-icons";
import {Study} from "@juniper/ui-core";
import {studyShortcodeFromPath} from "../study/StudyRouter";
import {useNavContext} from "./NavContextProvider";

/** renders the left navbar of admin tool */
const AdminSidebar = () => {
  const [open, setOpen] = useState(true)
  const { user } = useUser()
  const params = useParams()
  const studyShortcode = studyShortcodeFromPath(params['*'])
  const { portalList } = useNavContext()

  let studyList: Study[] = []
  if (portalList.length) {
    studyList = portalList.flatMap(portal => portal.portalStudies.map(ps => ps.study))
  }

  if (user.isAnonymous) {
    return <div></div>
  }
  const currentStudy = studyList.find(study => study.shortcode === studyShortcode)

  return <div style={{backgroundColor: '#333F52', height: '100vh', minWidth: open ? '250px' : '50px'}}
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
      { currentStudy && <StudySidebarOptions study={currentStudy}/> }
      <ul className="nav nav-pills flex-column mb-auto">
        {user.superuser && <li>
          <SidebarNavLink to="/users">All users</SidebarNavLink>
        </li>}
      </ul>
    </>}
  </div>;
}

const StudySidebarOptions = ({study}: {study: Study}) => {
  return <>
    <div className="text-white">
      {study.name}
      <CollapsableMenu header={'Research Coordination'} content={<ul className="list-unstyled">
        <li className="pb-2">Tasks</li>
        <li>Participant list</li>
      </ul>}/>
    </div>
  </>
}

function CollapsableMenu({header, content}: {header: React.ReactNode, content: React.ReactNode}) {
  const contentId = useId()
  const targetSelector  = `#${contentId}`
  return <div className="pt-3">
    <div>
      <button
          aria-controls={targetSelector}
          aria-expanded="true"
          className="btn-link btn w-100 py-2 px-0 d-flex fw-bold text-decoration-none text-white"
          data-bs-target={targetSelector}
          data-bs-toggle="collapse"
      >
        <span className="text-center" style={{ width: 30 }}>
          <FontAwesomeIcon icon={faChevronDown} className="hidden-when-collapsed"/>
          <FontAwesomeIcon icon={faChevronUp} className="hidden-when-expanded"/>
        </span>
        {header}
      </button>
    </div>
    <div className="collapse show" id={contentId} style={{paddingLeft: '30px'}}>
      {content}
    </div>
  </div>

}

/** renders a link in the sidebar with appropriate style and onClick handler to close the sidebar when clicked */
export function SidebarNavLink(props: NavLinkProps) {
  return (
      <NavLink
          {...props}
          className="nav-link"
          style={{ ...props.style, color: '#fff' }}
      />
  )
}

export default AdminSidebar
