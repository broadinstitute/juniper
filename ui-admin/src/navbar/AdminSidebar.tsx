import React, {useState} from 'react'
import { useUser } from '../user/UserProvider'
import {Link, NavLink, NavLinkProps} from "react-router-dom";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowLeft, faArrowRight} from "@fortawesome/free-solid-svg-icons";
import {Accordion} from "react-bootstrap";
import {Portal, Study} from "@juniper/ui-core";
import {participantListPath} from "../study/StudyEnvironmentRouter";

/** renders the left navbar of admin tool */
const AdminSidebar = ({portal, study, studyList}: {portal?: Portal, study?: Study, studyList?: Study[]}) => {
  const [open, setOpen] = useState(true)
  const { user } = useUser()

  if (user.isAnonymous) {
    return <div></div>
  }

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
      <Accordion defaultActiveKey={['0']} alwaysOpen >
        <Accordion.Item eventKey="0" key="0">
            <Accordion.Header>Research Coordination</Accordion.Header>
            <Accordion.Body>
              <ul className="list-unstyled ps-2">
                <li>Tasks</li>
                <li>
                  <Link to={participantListPath('ourhealth', 'ourheart', 'live')}>
                    Participant List
                  </Link>
                </li>
              </ul>
            </Accordion.Body>
          </Accordion.Item>
      </Accordion>

      <ul className="nav nav-pills flex-column mb-auto">
        {user.superuser && <li>
          <SidebarNavLink to="/users">All users</SidebarNavLink>
        </li>}
      </ul>
    </>}
  </div>;
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
