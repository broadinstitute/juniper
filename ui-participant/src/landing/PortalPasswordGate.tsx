import React, {useState} from "react";
import {Portal} from "../api/api";

const PORTAL_PASSWORD_KEY = "portal_password"

/**
 * if the portal config specifies a password, prevent showing the rest of the portal
 * We store the password in session storage so that it survives page refreshes, but is not permanent
 * to allow easy testing and remind people it is still there!
 * */
export default function PortalPasswordGate({portal, children}: { portal: Portal, children: React.ReactNode }) {
  const [typedPassword, setTypedPassword] = useState("")
  const password = sessionStorage.getItem(PORTAL_PASSWORD_KEY)
  const portalConfig = portal.portalEnvironments[0].portalEnvironmentConfig

  /** updates the sessionStorage and forces a rerender */
  function submitPassword(event: React.UIEvent<HTMLFormElement>) {
    event.preventDefault()
    sessionStorage.setItem(PORTAL_PASSWORD_KEY, typedPassword)
    if (typedPassword !== portalConfig.password) {
      alert("incorrect password")
    }
    setTypedPassword("")
  }

  if (portalConfig.passwordProtected && password !== portalConfig.password) {
    return <div className="row justify-content-center">
      <form onSubmit={submitPassword} className="col-md-6 p-5 text-center">
        <div className="mb-4">
          A password is required to view this site
        </div>
        <input type="text" size={20} value={typedPassword}
               onChange={(e) => setTypedPassword(e.currentTarget.value)}/><br/>
        <button type="submit" className="btn btn-primary">Submit</button>
      </form>
    </div>
  }
  return <>{children}</>
}
