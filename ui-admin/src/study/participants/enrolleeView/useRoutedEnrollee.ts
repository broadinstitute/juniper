import { useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { StudyParams } from '../../StudyRouter'
import Api, { Enrollee } from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'

export type EnrolleeParams = StudyParams & {
  enrolleeShortcodeOrId: string,
  surveyStableId: string,
  consentStableId: string
}

/** Handles loading a specific enrollee from the server, based on URL params */
export default function useRoutedEnrollee(studyEnvContext: StudyEnvContextT) {
  const { portal, study, currentEnv } = studyEnvContext
  const params = useParams<EnrolleeParams>()
  const enrolleeShortcodeOrId = params.enrolleeShortcodeOrId as string
  const [enrollee, setEnrollee] = useState<Enrollee>()
  const navigate = useNavigate()
  const location = useLocation()

  const { isLoading, reload } = useLoadingEffect(async () => {
    let enrollee = location?.state?.enrollee
    if (!enrollee) {
      enrollee = await Api.getEnrollee(portal.shortcode, study.shortcode,
        currentEnv.environmentName, enrolleeShortcodeOrId)
    } else {
      // clear the state so this page will update if we navigate to a new enrollee or refresh
      window.history.replaceState({}, '')
    }
    if (enrollee.shortcode != enrolleeShortcodeOrId) {
      // we got an id -- reroute to the shortcode path
      navigate(location.pathname.replace(enrollee.id, enrollee.shortcode), { replace: true, state: { enrollee } })
    }
    setEnrollee(enrollee)
  }, [enrolleeShortcodeOrId])

  return { isLoading, enrollee, reload }
}
