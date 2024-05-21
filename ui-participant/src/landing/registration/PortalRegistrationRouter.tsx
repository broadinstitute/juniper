import Api, { Portal, Survey } from 'api/api'
import { Route, Routes, useNavigate } from 'react-router-dom'
import React, { useEffect, useState } from 'react'
import { useUser } from 'providers/UserProvider'
import Ineligible from './Ineligible'
import PreRegistration from './Preregistration'
import Registration from './Registration'
import { usePreRegResponseId } from 'browserPersistentState'
import RegistrationUnauthed from './RegistrationUnauthed'

export type RegistrationContextT = {
  preRegSurvey?: Survey,
  preRegResponseId: string | null,
  updatePreRegResponseId: (newId: string | null) => void
}

/**
 * handles managing any preregistration response id and routing to pre-reg pages as needed.
 * If a valid preregId exists, this will redirect to the registration page.  If not, it will route to
 * the prereg page */
export default function PortalRegistrationRouter({
  portal,
  returnTo = '/hub'
}: { portal: Portal, returnTo: string | null }) {
  const [preRegResponseId, setPreRegResponseId] = usePreRegResponseId()
  const portalEnv = portal.portalEnvironments[0]
  const preRegSurvey = portalEnv.preRegSurvey
  const [preRegSatisfied, setPreRegSatisfied] = useState(!portalEnv.preRegSurvey)
  const navigate = useNavigate()
  const { user } = useUser()

  /** updates the state and localStorage */
  function updatePreRegResponseId(preRegId: string | null) {
    setPreRegSatisfied(!!preRegId)
    setPreRegResponseId(preRegId)
  }


  useEffect(() => {
    // if there's a preRegResponseId on initial load (because it was in local storage) validate it and then redirect
    if (preRegResponseId) {
      Api.confirmPreRegResponse(preRegResponseId).then(() => {
        //this is a valid pre-reg, redirect to the registration page
        setPreRegSatisfied(true)
      }).catch(() => {
        updatePreRegResponseId(null)
      })
    }
    // when this component is unmounted, clear the localstorage
    return () => {
      updatePreRegResponseId(null)
    }
  }, [])

  useEffect(() => {
    if (user) {
      alert('You are already registered')
      if (returnTo != null) {
        navigate(returnTo)
      }
      return
    }
    // navigate according to whether they need to complete a preReg survey
    if (preRegSatisfied) {
      navigate('register', { replace: true })
    } else {
      navigate('prereg', { replace: true })
    }
  }, [preRegSatisfied])

  const registrationContext = {
    preRegSurvey,
    preRegResponseId,
    updatePreRegResponseId
  }

  const registrationComponent = process.env.REACT_APP_UNAUTHED_LOGIN ?
    <RegistrationUnauthed registrationContext={registrationContext} returnTo={returnTo}/> :
    <Registration/>

  return <Routes>
    <Route path="ineligible" element={<Ineligible/>}/>
    <Route path="preReg" element={<PreRegistration registrationContext={registrationContext}/>}/>
    <Route path="register" element={registrationComponent}/>
  </Routes>
}
