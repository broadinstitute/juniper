import Api, { Portal, Survey } from 'api/api'
import { Outlet, useNavigate, useOutletContext } from 'react-router-dom'
import React, { useEffect, useState } from 'react'

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user isn't signed in yet (since they don't have an account), so local storage is the best way to keep this. */
const PREREG_ID_STORAGE_KEY = 'preRegResponseId'

export type RegistrationContextT = {
  preRegSurvey: Survey | null,
  preRegResponseId: string | null,
  updatePreRegResponseId: (newId: string | null) => void
}

/** convenience function for using the outlet context */
export function useRegistrationOutlet() {
  return useOutletContext<RegistrationContextT>()
}

/**
 * handles selecting/loading the correct study environment, and managing the preregistration response id.
 * If a valid preregId exists, this will redirect to the registration page.  If not, it will route to
 * the prereg page */
export default function PortalRegistrationOutlet({ portal }: { portal: Portal }) {
  const [preRegResponseId, setPreRegResponseId] = useState<string | null>(localStorage.getItem(PREREG_ID_STORAGE_KEY))
  const navigate = useNavigate()

  /** updates the state and localStorage */
  function updatePreRegResponseId(preRegId: string | null) {
    if (!preRegId) {
      localStorage.removeItem(PREREG_ID_STORAGE_KEY)
    } else {
      localStorage.setItem(PREREG_ID_STORAGE_KEY, preRegId)
      navigate('register', { replace: true })
    }
    setPreRegResponseId(preRegId)
  }

  const portalEnv = portal.portalEnvironments[0]
  if (!portalEnv) {
    return <div>No matching portal environment</div>
  }
  const preRegSurvey = portalEnv.preRegSurvey

  useEffect(() => {
    if (preRegResponseId) {
      Api.confirmPortalPreReg(preRegResponseId).then(() => {
        //this is a valid pre-reg, redirect to the registration page
        navigate('register', { replace: true })
      }).catch(() => {
        updatePreRegResponseId(null)
        navigate('preReg', { replace: true })
      })
    } else {
      navigate('preReg', { replace: true })
    }
  }, [])

  return <Outlet context={{
    preRegSurvey,
    studyShortcode: null, studyEnv: null, preRegResponseId, updatePreRegResponseId
  }}/>
}


