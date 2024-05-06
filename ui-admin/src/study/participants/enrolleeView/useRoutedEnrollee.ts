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
      // if we haven't already loaded the enrollee, get it from the server
      enrollee = await Api.getEnrollee(portal.shortcode, study.shortcode,
        currentEnv.environmentName, enrolleeShortcodeOrId)
    } else {
      // if we have, clear the state so the enrollee will be reloaded if the page is refreshed
      window.history.replaceState({}, '')
    }
    if (enrollee.shortcode != enrolleeShortcodeOrId) {
      // the page was routed by id rather than shortcode--use the shortcode route instead
      // but put the enrollee in state so we don't double-fetch
      navigate(location.pathname.replace(enrollee.id, enrollee.shortcode), { replace: true, state: { enrollee } })
    }
    setEnrollee(enrollee)
  }, [enrolleeShortcodeOrId])

  return { isLoading, enrollee, reload }
}
