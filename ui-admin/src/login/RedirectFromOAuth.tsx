import { UserManager } from "oidc-client-ts";
import React, { useEffect } from "react";
import {getOidcConfig} from "../authConfig";

export const RedirectFromOAuth = () => {
  const userManager = new UserManager(getOidcConfig())

  const url = window.location.href
  const isSilent = window.location.pathname.startsWith('/redirect-from-oauth-silent')
  useEffect(() => {
    if (isSilent) {
      userManager.signinSilentCallback(url)
    } else {
      userManager.signinPopupCallback(url)
    }
  }, [])

  const spinnerSize = 54
  return <div>Loading...</div>
}