import React, {useEffect, useState} from 'react'
import {useUser} from "providers/UserProvider";
import {Outlet, useNavigate, useOutletContext, useParams} from "react-router-dom";
import {usePortalEnv} from "providers/PortalProvider";
import {ParticipantUser, StudyEnvironment} from "api/api";
import LandingNavbar from "../../landing/LandingNavbar";

/** store the preregistration response id in local storage so a page refresh does not lose their progress.
 * The user might not be signed in yet (since they don't have an account),
 * so local storage is the best way to keep this. */
const PRE_ENROLL_ID_KEY = 'preEnrollResponseId'

export type StudyEnrollContext = {
  user: ParticipantUser,
  studyEnv: StudyEnvironment,
  studyShortcode: string,
  preEnrollResponseId: string | null,
  updatePreEnrollResponseId: (newId: string | null) => void
}

export function useEnrollContext() {
  return useOutletContext<StudyEnrollContext>()
}

export default function StudyEnrollOutlet() {
  const studyShortcode = useParams().studyShortcode
  const {portal} = usePortalEnv()
  const matchedStudy = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
  const studyEnv = matchedStudy?.studyEnvironments[0]
  if (!studyEnv || !studyShortcode) {
    return <div>no matching study</div>
  }
  return <StudyEnrollOutletMatched studyEnv={studyEnv} studyShortcode={studyShortcode}/>
}

function StudyEnrollOutletMatched({studyEnv, studyShortcode}: { studyEnv: StudyEnvironment, studyShortcode: string }) {
  const {user} = useUser()
  const navigate = useNavigate()
  const [preEnrollResponseId, setPreEnrollResponseId] = useState<string | null>(localStorage.getItem(PRE_ENROLL_ID_KEY))

  /** updates the state and localStorage */
  function updatePreEnrollResponseId(preRegId: string | null) {
    if (!preRegId) {
      localStorage.removeItem(PRE_ENROLL_ID_KEY)
    } else {
      localStorage.setItem(PRE_ENROLL_ID_KEY, preRegId)
      navigate('consent')
    }
    setPreEnrollResponseId(preRegId)
  }

  useEffect(() => {
    if (studyEnv.preEnrollSurvey) {
      navigate('preEnroll')
    } else {
      navigate('consent')
    }
  }, [])
  return <div>
    {user.isAnonymous && <LandingNavbar/>}
    <Outlet context={{
      studyShortcode, studyEnv, user, preEnrollResponseId, updatePreEnrollResponseId
    }}/>
  </div>

}


