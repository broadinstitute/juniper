import React from "react";
import {useUser} from "../providers/UserProvider";
import {usePortalEnv} from "../providers/PortalProvider";
import {NavLink} from "react-router-dom";

export default function HubPage() {
  const {user} = useUser()
  const {portal} = usePortalEnv()
  const portalStudies = portal.portalStudies

  return <div>
    <h5 className="text-center">Hub</h5>
    <div>
      <ul>
        {portalStudies.map(portalStudy => <li>
          <h6>{portalStudy.study.name}</h6>
          <NavLink to={`/studies/${portalStudy.study.shortcode}/join`}>Join</NavLink>
        </li>)}
      </ul>
    </div>
  </div>
}
