import React from "react";
import {NavLink} from "react-router-dom";
import {NavbarItem, getImageUrl} from "api/api";
import {usePortalEnv} from "providers/PortalProvider";

export default function LandingNavbar() {
  const {localContent} = usePortalEnv()
  const navLinks = localContent.navbarItems

  return <nav className="LandingNavbar navbar navbar-expand-lg navbar-light">
    <div className="container-fluid">
      <NavLink to="/" className="navbar-brand">
        <img className="Navbar-logo" style={{maxHeight: '30px'}}
             src={getImageUrl(localContent.navLogoShortcode)} alt="logo"/>
      </NavLink>
      <div className="collapse navbar-collapse" id="navbarNavDropdown">
        <ul className="navbar-nav">
          { navLinks.map((navLink: any, index: number) => <li key={index} >
            <CustomNavLink navLink={navLink}/>
          </li>)}
        </ul>
        <ul className="navbar-nav ms-auto">
          <li className="nav-item dropdown">
            <NavLink className="nav-link" to="login">Login</NavLink>
          </li>
        </ul>
      </div>
    </div>
  </nav>
}


export function CustomNavLink({navLink}: {navLink: NavbarItem}) {
  function mailingList(navLinkObj: any) {
    alert("mailing list")
  }

  if (navLink.itemType === 'INTERNAL') {
    return <NavLink to={navLink.htmlPage.path} className="nav-link ms-3">{navLink.label}</NavLink>
  } else if (navLink.itemType === 'MAILING_LIST') {
    return <a role="button" className="nav-link ms-3" onClick={() => mailingList(navLink)}>{navLink.label}</a>
  } else if (navLink.itemType === 'EXTERNAL') {
    return <a href={navLink.externalLink} className="nav-link ms-3" target="_blank">{navLink.label}</a>
  }
  return <></>
}
