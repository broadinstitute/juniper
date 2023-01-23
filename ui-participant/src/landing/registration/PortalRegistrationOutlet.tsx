import Api, { Portal, Survey } from 'api/api'
import { Outlet, useNavigate, useOutletContext } from 'react-router-dom'
import React, { useEffect, useState } from 'react'
import { useUser } from '../../providers/UserProvider'

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
 * handles managing any preregistration response id and routing to pre-reg pages as needed.
 * If a valid preregId exists, this will redirect to the registration page.  If not, it will route to
 * the prereg page */
export default function PortalRegistrationOutlet({ portal }: { portal: Portal }) {
  const [preRegResponseId, setPreRegResponseId] = useState<string | null>(localStorage.getItem(PREREG_ID_STORAGE_KEY))
  const navigate = useNavigate()
  const { user } = useUser()

  /** updates the state and localStorage */
  function updatePreRegResponseId(preRegId: string | null) {
    if (!preRegId) {
      localStorage.removeItem(PREREG_ID_STORAGE_KEY)
    } else {
      localStorage.setItem(PREREG_ID_STORAGE_KEY, preRegId)
      navigate('register')
    }
    setPreRegResponseId(preRegId)
  }

  const portalEnv = portal.portalEnvironments[0]
  if (!portalEnv) {
    return <div>No matching portal environment</div>
  }
  const preRegSurvey = portalEnv.preRegSurvey

  useEffect(() => {
    if (!user.isAnonymous) {
      alert('You are already registered')
      navigate('/')
      return
    }
    // if there's a preRegREsponseId on initial load (because it was in local storage) validate it and then redirect
    if (preRegResponseId) {
      Api.confirmPortalPreReg(preRegResponseId).then(() => {
        //this is a valid pre-reg, redirect to the registration page
        navigate('register', { replace: true })
      }).catch(() => {
        updatePreRegResponseId(null)
        navigate('preReg', { replace: true })
      })
    } else {
      // otherwise, go to prereg if it exists
      if (preRegSurvey) {
        navigate('prereg', { replace: true })
      } else {
        navigate('register', { replace: true })
      }
    }
    // when this component is unmounted, clear the localstorage
    return () => {
      localStorage.removeItem(PREREG_ID_STORAGE_KEY)
    }
  }, [])

  return <Outlet context={{
    preRegSurvey,
    studyShortcode: null, studyEnv: null, preRegResponseId, updatePreRegResponseId
  }}/>
}


