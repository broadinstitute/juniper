import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { StudyParams } from '../../StudyRouter'
import Api, { Enrollee } from 'api/api'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'

export type EnrolleeParams = StudyParams & {
  enrolleeShortcode: string,
  surveyStableId: string,
  consentStableId: string
}

/** Handles loading a specific enrollee from the server, based on URL params */
export default function useRoutedEnrollee(studyEnvContext: StudyEnvContextT) {
  const { portal, study, currentEnv } = studyEnvContext
  const params = useParams<EnrolleeParams>()
  const enrolleeShortcode = params.enrolleeShortcode as string
  const [enrollee, setEnrollee] = useState<Enrollee>()

  const { isLoading, reload } = useLoadingEffect(async () => {
    const result = await Api.getEnrollee(
      portal.shortcode, study.shortcode, currentEnv.environmentName, enrolleeShortcode
    )
    setEnrollee(result)
  }, [enrolleeShortcode])

  return { isLoading, enrollee, reload }
}
