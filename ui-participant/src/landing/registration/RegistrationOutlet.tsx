import Api, {Portal, StudyEnvironment} from "../../api/api";
import {Outlet, useNavigate, useOutletContext, useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user isn't signed in yet (since they don't have an account), so local storage is the best way to keep this. */
const PREREG_ID_STORAGE_KEY = "preRegResponseId"

export type RegistrationContextT = {
  studyEnv: StudyEnvironment,
  studyShortcode: string,
  portalShortcode: string,
  preRegResponseId: string | null,
  updatePreRegResponseId: (newId: string | null) => void
}
export function useRegistrationOutlet() {
  return useOutletContext<RegistrationContextT>()
}

type StudyEnvironmentParams = {
  studyShortcode: string,
}

/** handles selecting/loading the correct study environment, and managing the preregistration response id.
 * If a valid preregId exists, this will redirect to the registration page.  If not, it will route to
 * the prereg page */
export default function RegistrationOutlet({portal}: {portal: Portal}) {
  const [preRegResponseId, setPreRegResponseId] = useState<string | null>(localStorage.getItem(PREREG_ID_STORAGE_KEY))
  const params = useParams<StudyEnvironmentParams>()
  const navigate = useNavigate()
  let studyShortcode: string | undefined = params.studyShortcode

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

  if (!studyShortcode) {
    return <div>Must specify a study shortcode</div>
  }

  const study = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
  if (!study) {
    return <div>No matching study for {studyShortcode}</div>
  }
  const studyEnv = study.studyEnvironments[0]
  if (!studyEnv) {
    return <div>No matching environment for {studyShortcode}</div>
  }
  studyEnv.studyShortcode = studyShortcode

  useEffect(() => {
    if (preRegResponseId) {
      Api.confirmPreReg(portal.shortcode, studyEnv.studyShortcode, studyEnv.environmentName,
        preRegResponseId).then(() => {
        //this is a valid pre-reg, redirect to the registration page
        navigate('register')
      }).catch(() => {
        updatePreRegResponseId(null)
        navigate('preReg')
      })
    } else {
      navigate('preReg')
    }
  }, [])

  return <Outlet context={{portalShortcode: portal.shortcode,
    studyShortcode, studyEnv, preRegResponseId, updatePreRegResponseId}}/>
}


