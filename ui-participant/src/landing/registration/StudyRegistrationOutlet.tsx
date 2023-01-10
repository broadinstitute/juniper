import Api, {Portal} from '../../api/api'
import {Outlet, useNavigate, useParams} from 'react-router-dom'
import React, {useEffect, useState} from 'react'

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user isn't signed in yet (since they don't have an account), so local storage is the best way to keep this. */
const PREREG_ID_STORAGE_KEY = 'portalPreRegResponseId'

type StudyEnvironmentParams = {
  studyShortcode: string

}

/**
 * handles selecting/loading the correct study environment, and managing the preregistration response id.
 * If a valid preregId exists, this will redirect to the registration page.  If not, it will route to
 * the prereg page */
export default function StudyRegistrationOutlet({portal}: { portal: Portal }) {
  const [preRegResponseId, setPreRegResponseId] = useState<string | null>(localStorage.getItem(PREREG_ID_STORAGE_KEY))
  const params = useParams<StudyEnvironmentParams>()
  const navigate = useNavigate()
  const studyShortcode: string | undefined = params.studyShortcode

  /** updates the state and localStorage */
  function updatePreRegResponseId(preRegId: string | null) {
    if (!preRegId) {
      localStorage.removeItem(PREREG_ID_STORAGE_KEY)
    } else {
      localStorage.setItem(PREREG_ID_STORAGE_KEY, preRegId)
      navigate('register', {replace: true})
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
      Api.confirmStudyPreReg(preRegResponseId, studyEnv.studyShortcode).then(() => {
        //this is a valid pre-reg, redirect to the registration page
        navigate('register', {replace: true})
      }).catch(() => {
        updatePreRegResponseId(null)
        navigate('preReg', {replace: true})
      })
    } else {
      navigate('preReg', {replace: true})
    }
  }, [])

  return <Outlet context={{
    portalShortcode: portal.shortcode,
    studyShortcode, studyEnv, preRegResponseId, updatePreRegResponseId
  }}/>
}


